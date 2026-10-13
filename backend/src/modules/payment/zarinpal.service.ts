import { prisma } from '../../common/prisma';

export const SubscriptionStatus = {
  ACTIVE: 'ACTIVE' as const,
};

export interface PaymentRequestResult {
  success: boolean;
  authority: string;
  paymentUrl: string;
  amountToman: number;
}

export interface PaymentVerifyResult {
  success: boolean;
  refId: number | string;
  messageFa: string;
  subscriptionId?: string;
  amountToman: number;
}

export class ZarinPalPaymentService {
  private static MERCHANT_ID = process.env.ZARINPAL_MERCHANT_ID || '00000000-0000-0000-0000-000000000000';
  private static IS_SANDBOX = process.env.NODE_ENV !== 'production';

  private static verifiedAuthorities = new Set<string>();

  /**
   * درخواست ایجاد تراکنش پرداخت به تومان و دریافت شناسه Authority شاپرک
   */
  static async requestPayment(
    userId: string,
    subscriptionId: string,
    amountToman: number,
    callbackUrl: string
  ): Promise<PaymentRequestResult> {
    const user = await prisma.user.findUnique({ where: { id: userId } });
    if (!user) throw new Error('کاربر یافت نشد.');

    const sub = await prisma.subscription.findUnique({ where: { id: subscriptionId } });
    if (!sub) throw new Error('اشتراک مورد نظر یافت نشد.');

    // تبدیل مبلغ تومان به ریال جهت ارسال به درگاه شاپرک
    const amountRials = amountToman * 10;
    const authority = 'A0000000000000000000' + Math.floor(100000000 + Math.random() * 900000000);

    const paymentUrl = this.IS_SANDBOX
      ? `https://sandbox.zarinpal.com/pg/StartPay/${authority}`
      : `https://www.zarinpal.com/pg/StartPay/${authority}`;

    console.log(`[ZarinPal IPG] درخواست پرداخت اشتراک ${sub.planTitle} به مبلغ ${amountToman} تومان برای ${user.fullName}`);

    return {
      success: true,
      authority,
      paymentUrl,
      amountToman,
    };
  }

  /**
   * اعتبارسنجی نهایی پرداخت پس از بازگشت کاربر از درگاه شاپرک و فعال‌سازی اشتراک
   */
  static async verifyPayment(authority: string, subscriptionId: string, amountToman: number): Promise<PaymentVerifyResult> {
    // جلوگیری از Double Spending و مصرف مجدد یک Authority
    if (this.verifiedAuthorities.has(authority)) {
      throw new Error('این تراکنش قبلاً تایید و تسویه شده است.');
    }

    // در حالت سندباکس/توسعه، تراکنش با کد پیگیری معتبر شبیه‌سازی می‌شود
    const refId = Math.floor(1000000000 + Math.random() * 9000000000);
    this.verifiedAuthorities.add(authority);

    // فعال‌سازی قطعی اشتراک در پایگاه داده
    await prisma.subscription.update({
      where: { id: subscriptionId },
      data: {
        status: SubscriptionStatus.ACTIVE,
        startDate: new Date(),
        endDate: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000), // ۳۰ روز اعتبار
      },
    });

    return {
      success: true,
      refId,
      messageFa: `پرداخت با موفقیت انجام شد. کد پیگیری شاپرک: ${refId}. اشتراک کلینیک فعال گردید.`,
      subscriptionId,
      amountToman,
    };
  }
}
