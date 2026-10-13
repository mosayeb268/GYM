import { prisma } from '../../common/prisma';
import { PaymentMethod, InvoiceStatus, SubscriptionType, SubscriptionStatus } from '@prisma/client';

export interface PackageTier {
  id: string;
  titleFa: string;
  type: SubscriptionType;
  priceToman: number;
  totalSessions: number | null;
  validityDays: number;
  includesClinical: boolean;
  trainerCommissionPercent: number;
}

export const OFFICIAL_CLINIC_PACKAGES: PackageTier[] = [
  {
    id: 'PKG_MONTHLY_UNLIMITED',
    titleFa: 'اشتراک ۱ ماهه نامحدود بدنسازی عمومی',
    type: SubscriptionType.TIME_BASED,
    priceToman: 1850000,
    totalSessions: null,
    validityDays: 30,
    includesClinical: false,
    trainerCommissionPercent: 30,
  },
  {
    id: 'PKG_12_SESSIONS_FITNESS',
    titleFa: 'پکیج ۱۲ جلسه‌ای فیتنس و تناسب اندام',
    type: SubscriptionType.SESSION_BASED,
    priceToman: 1450000,
    totalSessions: 12,
    validityDays: 45,
    includesClinical: false,
    trainerCommissionPercent: 40,
  },
  {
    id: 'PKG_24_SESSIONS_FITNESS',
    titleFa: 'پکیج ۲۴ جلسه‌ای فیتنس و افزایش حجم',
    type: SubscriptionType.SESSION_BASED,
    priceToman: 2650000,
    totalSessions: 24,
    validityDays: 75,
    includesClinical: false,
    trainerCommissionPercent: 45,
  },
  {
    id: 'PKG_12_CORRECTIVE_VIP',
    titleFa: 'پکیج ۱۲ جلسه‌ای تخصصی حرکات اصلاحی بالینی (نظارت دکتر قدم‌پور)',
    type: SubscriptionType.SESSION_BASED,
    priceToman: 3850000,
    totalSessions: 12,
    validityDays: 60,
    includesClinical: true,
    trainerCommissionPercent: 60,
  },
  {
    id: 'PKG_24_HYBRID_VIP',
    titleFa: 'پکیج ۲۴ جلسه‌ای ترکیبی VIP: اصلاحی بالینی + هایپرتروفی',
    type: SubscriptionType.HYBRID,
    priceToman: 5200000,
    totalSessions: 24,
    validityDays: 90,
    includesClinical: true,
    trainerCommissionPercent: 65,
  },
];

export class AccountingService {
  /**
   * صدور فاکتور و فعال‌سازی اشتراک جدید برای مراجع
   */
  static async purchaseSubscription(params: {
    userId: string;
    packageId: string;
    paymentMethod: PaymentMethod;
    paymentRef?: string;
    discountToman?: number;
    trainerId?: string;
    notesFa?: string;
  }) {
    const pkg = OFFICIAL_CLINIC_PACKAGES.find((p) => p.id === params.packageId);
    if (!pkg) {
      throw new Error('پکیج انتخابی نامعتبر است.');
    }

    const discount = params.discountToman || 0;
    const finalAmountToman = Math.max(0, pkg.priceToman - discount);

    const startDate = new Date();
    const endDate = new Date(Date.now() + pkg.validityDays * 24 * 60 * 60 * 1000);

    // ۱. ثبت اشتراک در پایگاه داده
    const subscription = await prisma.subscription.create({
      data: {
        userId: params.userId,
        planTitle: pkg.titleFa,
        type: pkg.type,
        status: SubscriptionStatus.ACTIVE,
        startDate,
        endDate,
        totalSessions: pkg.totalSessions,
        remainingSessions: pkg.totalSessions,
        includesClinical: pkg.includesClinical,
        priceToman: finalAmountToman,
      },
    });

    // ۲. تولید شماره فاکتور رسمی (مانند: INV-1403-7842)
    const randomSeq = Math.floor(1000 + Math.random() * 9000);
    const invoiceNumber = `INV-1403-${randomSeq}`;

    const invoice = await prisma.invoice.create({
      data: {
        invoiceNumber,
        userId: params.userId,
        subscriptionId: subscription.id,
        titleFa: `شهریه ${pkg.titleFa}`,
        amountToman: finalAmountToman,
        discountToman: discount,
        paymentMethod: params.paymentMethod,
        paymentRef: params.paymentRef || `POS-${Date.now().toString().slice(-6)}`,
        status: InvoiceStatus.PAID,
        paidAt: new Date(),
        notesFa: params.notesFa,
      },
    });

    // ۳. محاسبه و ثبت سهم مربی / متخصص اصلاحی (در صورت انتصاب مربی)
    if (params.trainerId) {
      const commissionAmount = Math.round((finalAmountToman * pkg.trainerCommissionPercent) / 100);
      await prisma.commissionSplit.create({
        data: {
          trainerId: params.trainerId,
          subscriptionId: subscription.id,
          trainerPercent: pkg.trainerCommissionPercent,
          amountToman: commissionAmount,
        },
      });
    }

    return {
      success: true,
      subscription,
      invoice,
    };
  }

  /**
   * گزارش مالی جامع روزانه و هفتگی به تفکیک روش پرداخت
   */
  static async getFinancialSummary() {
    const invoices = await prisma.invoice.findMany({
      where: { status: InvoiceStatus.PAID },
      include: { user: true },
      orderBy: { issuedAt: 'desc' },
      take: 50,
    });

    let totalRevenueToman = 0;
    let posTotal = 0;
    let cashTotal = 0;
    let onlineTotal = 0;

    for (const inv of invoices) {
      totalRevenueToman += inv.amountToman;
      if (inv.paymentMethod === PaymentMethod.POS_TERMINAL) posTotal += inv.amountToman;
      if (inv.paymentMethod === PaymentMethod.CASH) cashTotal += inv.amountToman;
      if (inv.paymentMethod === PaymentMethod.ONLINE_GATEWAY) onlineTotal += inv.amountToman;
    }

    return {
      totalRevenueToman,
      breakdown: {
        posTerminalToman: posTotal,
        cashToman: cashTotal,
        onlineGatewayToman: onlineTotal,
      },
      recentInvoices: invoices.map((inv) => ({
        invoiceNumber: inv.invoiceNumber,
        clientName: inv.user.fullName,
        amountToman: inv.amountToman,
        paymentMethod: inv.paymentMethod,
        dateUtc: inv.issuedAt,
      })),
    };
  }
}
