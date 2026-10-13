import { ISmsProvider, SendOtpOptions, SendMessageOptions } from './sms.interface';

export class KavenegarProvider implements ISmsProvider {
  name = 'Kavenegar';
  private apiKey: string;

  constructor(apiKey: string) {
    this.apiKey = apiKey;
  }

  async sendOtp(options: SendOtpOptions): Promise<boolean> {
    try {
      const template = options.template || 'kinetic_otp';
      const url = `https://api.kavenegar.com/v1/${this.apiKey}/verify/lookup.json?receptor=${options.receptor}&token=${options.token}&template=${template}`;
      
      console.log(`[Kavenegar SMS] ارسال کد OTP به شماره ${options.receptor} با الگوی ${template}`);
      // در محیط عملیاتی با استفاده از fetch یا axios فراخوانی می‌شود
      return true;
    } catch (error) {
      console.error('[Kavenegar SMS] خطا در ارسال پیامک OTP:', error);
      return false;
    }
  }

  async sendMessage(options: SendMessageOptions): Promise<boolean> {
    try {
      console.log(`[Kavenegar SMS] ارسال پیامک متنی به ${options.receptor}: ${options.message}`);
      return true;
    } catch (error) {
      console.error('[Kavenegar SMS] خطا در ارسال پیامک:', error);
      return false;
    }
  }
}
