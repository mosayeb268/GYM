import { prisma } from '../../common/prisma';

export const Role = {
  ATHLETE_CLIENT: 'ATHLETE_CLIENT' as const,
};

export const SubscriptionStatus = {
  ACTIVE: 'ACTIVE' as const,
  EXPIRED: 'EXPIRED' as const,
  FROZEN: 'FROZEN' as const,
};

export class ClientManagementService {
  /**
   * ۱. ثبت‌نام حضوری مراجع جدید توسط متصدی پذیرش یا مربی
   */
  static async registerClient(params: {
    fullName: string;
    phoneNumber: string;
    nationalCode?: string;
    gender?: string;
    emergencyContact?: string;
    medicalHistory?: string;
    role?: Role;
  }) {
    // بررسی عدم وجود شماره تکراری
    const existing = await prisma.user.findUnique({
      where: { phoneNumber: params.phoneNumber },
    });

    if (existing) {
      throw new Error('کاربری با این شماره تلفن همراه قبلاً در سیستم ثبت شده است.');
    }

    const user = await prisma.user.create({
      data: {
        fullName: params.fullName,
        phoneNumber: params.phoneNumber,
        nationalCode: params.nationalCode,
        gender: params.gender || 'نامشخص',
        emergencyContact: params.emergencyContact,
        medicalHistory: params.medicalHistory,
        role: params.role || Role.ATHLETE_CLIENT,
      },
    });

    return {
      success: true,
      messageFa: `مراجع [${user.fullName}] با موفقیت در پرونده الکترونیک کلینیک ثبت گردید.`,
      user,
    };
  }

  /**
   * ۲. مدیریت و تنظیم دستی سهمیه جلسات ورزشکار به همراه ثبت لاگ تغییرات
   */
  static async adjustSessions(params: {
    subscriptionId: string;
    deltaSessions: number; // e.g. +2 یا -1
    reasonFa: string;
    adjustedByUserId: string;
  }) {
    const sub = await prisma.subscription.findUnique({
      where: { id: params.subscriptionId },
      include: { user: true },
    });

    if (!sub) {
      throw new Error('اشتراک مورد نظر یافت نشد.');
    }

    if (sub.remainingSessions === null) {
      throw new Error('این اشتراک بر پایه زمان (نامحدود) است و دارای سهمیه جلسه‌ای نمی‌باشد.');
    }

    const previousSessions = sub.remainingSessions;
    const newSessions = Math.max(0, previousSessions + params.deltaSessions);

    // به‌روزرسانی سهمیه و وضعیت اشتراک
    const updatedSub = await prisma.subscription.update({
      where: { id: sub.id },
      data: {
        remainingSessions: newSessions,
        status: newSessions === 0 ? SubscriptionStatus.EXPIRED : SubscriptionStatus.ACTIVE,
      },
    });

    // ثبت لاگ حسابرسی (Audit Trail Log)
    await prisma.sessionAdjustmentLog.create({
      data: {
        subscriptionId: sub.id,
        adjustedByUserId: params.adjustedByUserId,
        deltaSessions: params.deltaSessions,
        previousSessions,
        newSessions,
        reasonFa: params.reasonFa,
      },
    });

    return {
      success: true,
      athleteName: sub.user.fullName,
      previousSessions,
      newSessions,
      delta: params.deltaSessions,
      messageFa: `تعداد جلسات با موفقیت از ${previousSessions} به ${newSessions} جلسه تغییر یافت.`,
    };
  }

  /**
   * ۳. تعلیق / فریز هوشمند اشتراک به علت مصدومیت، بیماری یا مسافرت
   */
  static async freezeSubscription(params: {
    subscriptionId: string;
    freezeDays: number;
    reasonFa: string;
  }) {
    const sub = await prisma.subscription.findUnique({
      where: { id: params.subscriptionId },
    });

    if (!sub || sub.status !== SubscriptionStatus.ACTIVE) {
      throw new Error('اشتراک فعال جهت فریز یافت نشد.');
    }

    const allowedFreeze = sub.maxFreezeDays - sub.usedFreezeDays;
    if (params.freezeDays > allowedFreeze) {
      throw new Error(`حداکثر سقف مجاز فریز باقیمانده برای این پکیج ${allowedFreeze} روز می‌باشد.`);
    }

    const now = new Date();
    const freezeEndDate = new Date(Date.now() + params.freezeDays * 24 * 60 * 60 * 1000);
    // تمدید متناظر تاریخ پایان اعتبار اشتراک
    const newSubEndDate = new Date(sub.endDate.getTime() + params.freezeDays * 24 * 60 * 60 * 1000);

    await prisma.subscription.update({
      where: { id: sub.id },
      data: {
        endDate: newSubEndDate,
        usedFreezeDays: sub.usedFreezeDays + params.freezeDays,
      },
    });

    await prisma.freezeLog.create({
      data: {
        subscriptionId: sub.id,
        startDate: now,
        endDate: freezeEndDate,
        isApproved: true,
      },
    });

    return {
      success: true,
      freezeDays: params.freezeDays,
      newEndDate: newSubEndDate,
      messageFa: `اشتراک با موفقیت به مدت ${params.freezeDays} روز تعلیق شد و تاریخ انقضا تمدید گردید.`,
    };
  }

  /**
   * ۴. سیستم شناسایی ورزشکاران در معرض ریزش (عدم حضور بیش از ۷ روز) جهت ارسال پیامک بازگشت
   */
  static async getAbsenteeismAlerts(daysThreshold: number = 7) {
    const cutoffDate = new Date(Date.now() - daysThreshold * 24 * 60 * 60 * 1000);

    const activeClients = await prisma.user.findMany({
      where: {
        role: Role.ATHLETE_CLIENT,
        subscriptions: { some: { status: SubscriptionStatus.ACTIVE } },
      },
      include: {
        attendanceLogs: {
          orderBy: { checkedInAt: 'desc' },
          take: 1,
        },
      },
    });

    const absentClients = activeClients.filter((client) => {
      const lastCheckIn = client.attendanceLogs[0]?.checkedInAt;
      return !lastCheckIn || lastCheckIn < cutoffDate;
    });

    return absentClients.map((client) => {
      const lastCheckIn = client.attendanceLogs[0]?.checkedInAt;
      const daysSinceLastVisit = lastCheckIn
        ? Math.floor((Date.now() - lastCheckIn.getTime()) / (24 * 60 * 60 * 1000))
        : 'نامشخص';

      return {
        userId: client.id,
        fullName: client.fullName,
        phoneNumber: client.phoneNumber,
        daysAbsent: daysSinceLastVisit,
        crmSmsTemplate: `${client.fullName} عزیز، بیش از ${daysSinceLastVisit} روز است جای شما در کلینیک دکتر قدم‌پور خالی است. برای حفظ دستاوردهای اصلاح قامت، منتظر حضور گرم شما هستیم.`,
      };
    });
  }
}
