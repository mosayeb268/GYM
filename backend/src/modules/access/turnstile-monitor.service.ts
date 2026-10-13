import { prisma } from '../../common/prisma';

export class TurnstileMonitorService {
  /**
   * واکشی زنده تردد گیت ورودی برای صفحه مانیتورینگ متصدی پذیرش
   */
  static async getLiveAttendanceFeed(limit: number = 20) {
    const logs = await prisma.attendanceLog.findMany({
      include: {
        user: {
          include: {
            subscriptions: {
              where: { status: 'ACTIVE' },
              take: 1,
            },
            lockerAllocations: {
              where: { isActive: true },
              take: 1,
            },
          },
        },
      },
      orderBy: { checkedInAt: 'desc' },
      take: limit,
    });

    return logs.map((log) => {
      const activeSub = log.user.subscriptions[0];
      const activeLocker = log.user.lockerAllocations[0];

      return {
        id: log.id,
        athleteName: log.user.fullName,
        phoneNumber: log.user.phoneNumber,
        status: log.status, // SUCCESS, REJECTED
        reason: log.reason,
        timeUtc: log.checkedInAt,
        gateDeviceId: log.gateDeviceId,
        subscriptionPlan: activeSub?.planTitle || 'فاقد اشتراک فعال',
        remainingSessions: activeSub?.remainingSessions ?? 'نامحدود',
        assignedLocker: activeLocker ? activeLocker.lockerNumber : null,
      };
    });
  }

  /**
   * تخصیص خودکار شماره کمد هوشمند (Smart Locker) هنگام تردد موفق
   */
  static async allocateSmartLocker(userId: string): Promise<number> {
    // بررسی کمد فعال قبلی
    const existing = await prisma.lockerAllocation.findFirst({
      where: { userId, isActive: true },
    });
    if (existing) return existing.lockerNumber;

    // یافتن شماره کمدهای اشغال شده (ظرفیت فرضی ۱۰۰ کمد)
    const occupiedLockers = await prisma.lockerAllocation.findMany({
      where: { isActive: true },
      select: { lockerNumber: true },
    });
    const occupiedSet = new Set(occupiedLockers.map((l) => l.lockerNumber));

    let freeLockerNumber = 1;
    while (occupiedSet.has(freeLockerNumber) && freeLockerNumber <= 100) {
      freeLockerNumber++;
    }

    if (freeLockerNumber > 100) {
      freeLockerNumber = 999; // کمد اضطراری
    }

    await prisma.lockerAllocation.create({
      data: {
        userId,
        lockerNumber: freeLockerNumber,
        isActive: true,
      },
    });

    return freeLockerNumber;
  }

  /**
   * آزادسازی کمد هنگام خروج ورزشکار
   */
  static async releaseSmartLocker(userId: string) {
    await prisma.lockerAllocation.updateMany({
      where: { userId, isActive: true },
      data: {
        isActive: false,
        releasedAt: new Date(),
      },
    });
    return { success: true, messageFa: 'کمد با موفقیت تخلیه و آزاد شد.' };
  }
}
