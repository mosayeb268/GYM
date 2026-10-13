/**
 * رابط لایه انتزاعی درگاه‌های ارسال پیامک ایران
 * کلینیک ورزشی و حرکات اصلاحی دکتر قدم‌پور
 */
export interface SendOtpOptions {
  receptor: string;       // شماره موبایل مقصد: 0912xxxxxxx
  token: string;          // کد اعتبارسنجی OTP (مثلاً 5421)
  template?: string;      // نام الگوی تایید شده در پنل پیامک
}

export interface SendMessageOptions {
  receptor: string;
  message: string;
}

export interface ISmsProvider {
  name: string;
  sendOtp(options: SendOtpOptions): Promise<boolean>;
  sendMessage(options: SendMessageOptions): Promise<boolean>;
}
