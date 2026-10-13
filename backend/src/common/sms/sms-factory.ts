import { ISmsProvider, SendOtpOptions, SendMessageOptions } from './sms.interface';
import { KavenegarProvider } from './kavenegar.provider';

class MockConsoleSmsProvider implements ISmsProvider {
  name = 'MockConsole';

  async sendOtp(options: SendOtpOptions): Promise<boolean> {
    console.log(`\n======================================================`);
    console.log(`[پیامک آزمایشی کلینیک دکتر قدم‌پور]`);
    console.log(`گیرنده: ${options.receptor}`);
    console.log(`کد اعتبارسنجی ورود (OTP): [ ${options.token} ]`);
    console.log(`الگو: ${options.template || 'پیش‌فرض'}`);
    console.log(`اعتبار: ۱۲۰ ثانیه`);
    console.log(`======================================================\n`);
    return true;
  }

  async sendMessage(options: SendMessageOptions): Promise<boolean> {
    console.log(`\n[پیامک متنی ارسالی] گیرنده: ${options.receptor} | متن: ${options.message}\n`);
    return true;
  }
}

export class SmsServiceFactory {
  private static instance: ISmsProvider;

  static getProvider(): ISmsProvider {
    if (!this.instance) {
      const providerType = process.env.SMS_PROVIDER || 'MOCK';
      const kavenegarKey = process.env.KAVENEGAR_API_KEY;

      if (providerType === 'KAVENEGAR' && kavenegarKey && kavenegarKey !== 'your_kavenegar_api_key_here') {
        this.instance = new KavenegarProvider(kavenegarKey);
      } else {
        this.instance = new MockConsoleSmsProvider();
      }
    }
    return this.instance;
  }
}
