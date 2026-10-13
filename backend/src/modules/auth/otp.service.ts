import crypto from 'crypto';
import jwt from 'jsonwebtoken';
import { prisma } from '../../common/prisma';
import { SmsServiceFactory } from '../../common/sms/sms-factory';
import { Role } from '@prisma/client';

interface OtpRecord {
  code: string;
  expiresAt: number;
  attempts: number;
}

export class OtpService {
  private static otpStore = new Map<string, OtpRecord>();
  private static rateLimitStore = new Map<string, { count: number; resetAt: number }>();

  private static JWT_SECRET = process.env.JWT_SECRET || 'kinetic_care_jwt_super_secret_key_ghadampour_clinic';
  private static OTP_EXPIRATION_MS = 120 * 1000; // ۱۲۰ ثانیه

  /**
   * نرمال‌سازی شماره موبایل به فرمت ایرانی استاندارد
   */
  static normalizePhone(phone: string): string {
    const cleaned = phone.replace(/[\s\-\+]/g, '');
    if (cleaned.startsWith('98')) return '0' + cleaned.substring(2);
    if (cleaned.startsWith('0098')) return '0' + cleaned.substring(4);
    if (!cleaned.startsWith('0')) return '0' + cleaned;
    return cleaned;
  }

  /**
   * بررسی صحت الگوی شماره تلفن همراه ایران
   */
  static isValidPhone(phone: string): boolean {
    const normalized = this.normalizePhone(phone);
    return /^09[0-9]{9}$/.test(normalized);
  }

  /**
   * ایجاد و ارسال کد یکبار مصرف پیامکی (OTP)
   */
  static async requestOtp(rawPhone: string): Promise<{ success: boolean; messageFa: string; expiresInSeconds: number }> {
    const phone = this.normalizePhone(rawPhone);

    if (!this.isValidPhone(phone)) {
      throw new Error('فرمت شماره تلفن همراه نامعتبر است. شماره باید با ۰۹ شروع شود و ۱۱ رقم باشد.');
    }

    // بررسی محدودیت نرخ درخواست (Rate Limiting: حداکثر ۳ درخواست در ۱۰ دقیقه)
    const now = Date.now();
    const rateLimit = this.rateLimitStore.get(phone);
    if (rateLimit) {
      if (now < rateLimit.resetAt) {
        if (rateLimit.count >= 3) {
          const waitMinutes = Math.ceil((rateLimit.resetAt - now) / 60000);
          throw new Error(`تعداد درخواست‌های پیامک بیش از حد مجاز است. لطفاً ${waitMinutes} دقیقه دیگر تلاش کنید.`);
        }
        rateLimit.count++;
      } else {
        this.rateLimitStore.set(phone, { count: 1, resetAt: now + 10 * 60 * 1000 });
      }
    } else {
      this.rateLimitStore.set(phone, { count: 1, resetAt: now + 10 * 60 * 1000 });
    }

    // تولید کد تصادفی ۵ رقمی امن
    const otpCode = crypto.randomInt(10000, 99999).toString();
    const expiresAt = now + this.OTP_EXPIRATION_MS;

    this.otpStore.set(phone, {
      code: otpCode,
      expiresAt,
      attempts: 0,
    });

    // ارسال پیامک از طریق ارائه‌دهنده سرویس
    const smsProvider = SmsServiceFactory.getProvider();
    await smsProvider.sendOtp({
      receptor: phone,
      token: otpCode,
      template: process.env.SMS_PATTERN_OTP || 'kinetic_otp',
    });

    return {
      success: true,
      messageFa: 'کد تایید ۱۲۰ ثانیه‌ای با موفقیت پیامک شد.',
      expiresInSeconds: 120,
    };
  }

  /**
   * اعتبارسنجی کد OTP و صدور نشست امن JWT
   */
  static async verifyOtp(rawPhone: string, code: string): Promise<{ token: string; user: any; isNewUser: boolean }> {
    const phone = this.normalizePhone(rawPhone);
    const record = this.otpStore.get(phone);

    if (!record) {
      throw new Error('کد تاییدی برای این شماره یافت نشد یا منقضی شده است. لطفاً مجدداً درخواست کد دهید.');
    }

    if (Date.now() > record.expiresAt) {
      this.otpStore.delete(phone);
      throw new Error('کد تایید منقضی شده است. مهلت استفاده ۱۲۰ ثانیه می‌باشد.');
    }

    record.attempts++;
    if (record.attempts > 5) {
      this.otpStore.delete(phone);
      throw new Error('تعداد تلاش‌های ناموفق بیش از حد مجاز بود. لطفاً مجدداً کد دریافت کنید.');
    }

    if (record.code !== code.trim()) {
      throw new Error('کد وارد شده صحیح نمی‌باشد.');
    }

    // کد تایید مصرف شد
    this.otpStore.delete(phone);

    // واکشی یا ثبت‌نام خودکار کاربر
    let isNewUser = false;
    let user = await prisma.user.findUnique({
      where: { phoneNumber: phone },
      include: {
        subscriptions: {
          where: { status: 'ACTIVE' },
          take: 1,
        },
      },
    });

    if (!user) {
      isNewUser = true;
      user = await prisma.user.create({
        data: {
          phoneNumber: phone,
          fullName: 'ورزشکار جدید کلینیک',
          role: Role.ATHLETE_CLIENT,
        },
        include: {
          subscriptions: true,
        },
      });
    }

    // امضای توکن JWT
    const payload = {
      sub: user.id,
      phone: user.phoneNumber,
      role: user.role,
      fullName: user.fullName,
    };

    const token = jwt.sign(payload, this.JWT_SECRET, { expiresIn: '30d' });

    return {
      token,
      user,
      isNewUser,
    };
  }
}
