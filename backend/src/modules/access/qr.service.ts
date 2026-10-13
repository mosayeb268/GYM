import crypto from 'crypto';
import { prisma } from '../../common/prisma';

export type SubscriptionStatus = 'ACTIVE' | 'FROZEN' | 'EXPIRED';

export interface QrVerifyResult {
  allowed: boolean;
  messageFa: string;
  athleteName?: string;
  remainingSessions?: number | null;
  subscriptionPlan?: string;
}

export class QrTurnstileService {
  private static HMAC_SECRET = process.env.QR_HMAC_SECRET || 'kinetic_care_qr_hmac_secret_key_2026';
  private static usedTokens = new Set<string>(); // در محیط کلاستر با Redis SETNX جایگزین می‌شود
  private static MAX_TIME_DRIFT_SEC = 20;        // حداکثر ۲۰ ثانیه تلرانس زمانی

  /**
   * تولید بارکد پویای تردد بر پایه الگوریتم HMAC-SHA256
   * قالب: userId.timestamp.hmacSignature
   */
  static generateDynamicPass(userId: string): { qrData: string; validForSeconds: number } {
    const timestamp = Math.floor(Date.now() / 1000);
    const payload = `${userId}.${timestamp}`;
    const signature = crypto
      .createHmac('sha256', this.HMAC_SECRET)
      .update(payload)
      .digest('hex')
      .substring(0, 32);

    return {
      qrData: `${userId}.${timestamp}.${signature}`,
      validForSeconds: 15,
    };
  }

  /**
   * بررسی و اعتبارسنجی بارکد در گیت تردد یا گوشی متصدی پذیرش
   */
  static async verifyTurnstilePass(qrData: string, gateDeviceId: string = 'GATE_MAIN_01'): Promise<QrVerifyResult> {
    const parts = qrData.split('.');
    if (parts.length !== 3) {
      return { allowed: false, messageFa: 'ساختار بارکد مخدوش یا نامعتبر است.' };
    }

    const [userId, timestampStr, clientSignature] = parts;
    const clientTimestamp = parseInt(timestampStr, 10);
    if (isNaN(clientTimestamp) || clientTimestamp <= 0) {
      return { allowed: false, messageFa: 'برچسب زمانی بارکد نامعتبر است.' };
    }
    const now = Math.floor(Date.now() / 1000);

    // ۱. اعتبارسنجی امضای رمزنگاری HMAC-SHA256
    const expectedPayload = `${userId}.${clientTimestamp}`;
    const expectedSignature = crypto
      .createHmac('sha256', this.HMAC_SECRET)
      .update(expectedPayload)
      .digest('hex')
      .substring(0, 32);

    if (clientSignature !== expectedSignature) {
      await this.logAttendance(userId, gateDeviceId, 'REJECTED', 'امضای دیجیتال بارکد نامعتبر است.');
      return { allowed: false, messageFa: 'جعل بارکد شناسایی شد؛ دسترسی غیرمجاز است.' };
    }

    // ۲. بررسی پنجره زمانی و انقضا (حداکثر ۲۰ ثانیه)
    const timeDiff = Math.abs(now - clientTimestamp);
    if (timeDiff > this.MAX_TIME_DRIFT_SEC) {
      await this.logAttendance(userId, gateDeviceId, 'REJECTED', 'بارکد پویا منقضی شده است.');
      return { allowed: false, messageFa: 'بارکد منقضی شده است. لطفاً بارکد جدید دریافت نمایید.' };
    }

    // ۳. جلوگیری از حمله Replay Attack (مصرف مجدد بارکد اسکن شده)
    if (this.usedTokens.has(qrData)) {
      await this.logAttendance(userId, gateDeviceId, 'REJECTED', 'تلاش برای استفاده مجدد از بارکد مصرف‌شده.');
      return { allowed: false, messageFa: 'این بارکد قبلاً استفاده شده است و فاقد اعتبار است.' };
    }
    this.usedTokens.add(qrData);

    // پاکسازی توکن بعد از ۳۰ ثانیه از حافظه
    setTimeout(() => {
      this.usedTokens.delete(qrData);
    }, 30000);

    // ۴. بررسی پروفایل کاربر و اشتراک‌های فعال در دیتابیس
    const user = await prisma.user.findUnique({
      where: { id: userId },
      include: {
        subscriptions: {
          where: { status: SubscriptionStatus.ACTIVE },
          orderBy: { endDate: 'desc' },
          take: 1,
        },
      },
    });

    if (!user || !user.isActive) {
      await this.logAttendance(userId, gateDeviceId, 'REJECTED', 'کاربر غیرفعال یا مسدود است.');
      return { allowed: false, messageFa: 'حساب کاربری مراجع غیرفعال است.' };
    }

    const activeSub = user.subscriptions[0];
    if (!activeSub) {
      await this.logAttendance(userId, gateDeviceId, 'REJECTED', 'عدم وجود اشتراک فعال.');
      return { allowed: false, messageFa: 'اشتراک فعالی برای مراجع یافت نشد. لطفاً به پذیرش مراجعه فرمایید.' };
    }

    // بررسی تاریخ پایان اشتراک
    if (new Date() > activeSub.endDate) {
      await prisma.subscription.update({
        where: { id: activeSub.id },
        data: { status: SubscriptionStatus.EXPIRED },
      });
      await this.logAttendance(userId, gateDeviceId, 'REJECTED', 'انقضای تاریخ اشتراک.');
      return { allowed: false, messageFa: 'تاریخ اشتراک شما منقضی شده است.' };
    }

    // بررسی مانده جلسات در اشتراک‌های جلسه‌ای و هیبریدی
    if (activeSub.remainingSessions !== null && activeSub.remainingSessions <= 0) {
      await prisma.subscription.update({
        where: { id: activeSub.id },
        data: { status: SubscriptionStatus.EXPIRED },
      });
      await this.logAttendance(userId, gateDeviceId, 'REJECTED', 'اتمام جلسات اشتراک.');
      return { allowed: false, messageFa: 'تعداد جلسات پکیج شما به پایان رسیده است.' };
    }

    // کسر یک جلسه در صورت لزوم
    let updatedRemaining = activeSub.remainingSessions;
    if (activeSub.remainingSessions !== null && activeSub.remainingSessions > 0) {
      updatedRemaining = activeSub.remainingSessions - 1;
      await prisma.subscription.update({
        where: { id: activeSub.id },
        data: {
          remainingSessions: updatedRemaining,
          status: updatedRemaining === 0 ? SubscriptionStatus.EXPIRED : SubscriptionStatus.ACTIVE,
        },
      });
    }

    // ۵. ثبت ورود موفق در لاگ تردد
    await this.logAttendance(userId, gateDeviceId, 'SUCCESS', 'تردد مجاز با بارکد پویا.');

    return {
      allowed: true,
      messageFa: `خوش آمدید ${user.fullName}. تردد با موفقیت ثبت شد.`,
      athleteName: user.fullName,
      remainingSessions: updatedRemaining,
      subscriptionPlan: activeSub.planTitle,
    };
  }

  private static async logAttendance(userId: string, gateDeviceId: string, status: string, reason: string) {
    try {
      await prisma.attendanceLog.create({
        data: {
          userId,
          gateDeviceId,
          status,
          reason,
        },
      });
    } catch (e) {
      console.error('خطا در ثبت لاگ تردد:', e);
    }
  }
}
