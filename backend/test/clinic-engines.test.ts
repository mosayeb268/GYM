import { PersianBidiReshaper } from '../src/common/pdf/persian-bidi';
import { FoodExchangeService } from '../src/modules/nutrition/food-exchange.service';
import { QrTurnstileService } from '../src/modules/access/qr.service';
import { OtpService } from '../src/modules/auth/otp.service';
import { OFFICIAL_CLINIC_PACKAGES } from '../src/modules/accounting/accounting.service';

async function runTests() {
  console.log('=== شروع اجرای تست‌های خودکار موتورهای کلینیک دکتر قدم‌پور ===\n');
  let passed = 0;
  let failed = 0;

  function assert(condition: boolean, testName: string) {
    if (condition) {
      console.log(`[PASS] ✓ ${testName}`);
      passed++;
    } else {
      console.error(`[FAIL] ✗ ${testName}`);
      failed++;
    }
  }

  // ۱. تست شماره موبایل ایران
  assert(OtpService.isValidPhone('09123456789'), 'اعتبارسنجی شماره همراه ایرانی 09123456789');
  assert(OtpService.isValidPhone('+989123456789'), 'اعتبارسنجی شماره همراه با پیش‌شماره بین‌المللی +98');
  assert(!OtpService.isValidPhone('08123456789'), 'رد شماره‌های غیر موبایل 081...');
  assert(!OtpService.isValidPhone('0912345678'), 'رد شماره‌های کوتاه‌تر از ۱۱ رقم');

  // ۲. تست موتور بازآرایی حروف BiDi
  const samplePersian = 'کلینیک قدم‌پور';
  const reshaped = PersianBidiReshaper.reshape(samplePersian);
  assert(reshaped.length > 0 && reshaped !== samplePersian, 'اتصال حروف فارسی به اشکال چسبان Presentation Forms');

  // ۳. تست سیستم تبادل مواد غذایی
  const exchange = FoodExchangeService.calculateExchange('سینه مرغ پخته', 'ماهی قزل‌آلا', 150);
  assert(exchange.targetPortionGrams === 180, 'تبدیل ۱۵۰ گرم سینه مرغ به ۱۸۰ گرم ماهی قزل‌آلا');
  assert(exchange.exchangeGroupFa === 'پروتئین خالص', 'تطابق گروه درشت‌مغذی پروتئین خالص');

  // ۴. تست بارکد پویای تردد HMAC
  const dummyUserId = 'athlete-test-123';
  const pass = QrTurnstileService.generateDynamicPass(dummyUserId);
  assert(pass.qrData.split('.').length === 3, 'تولید بارکد تردد ۳ قسمتی (userId.timestamp.hmac)');
  assert(pass.validForSeconds === 15, 'اعتبار ۱۵ ثانیه‌ای بارکد پویا');

  // ۵. تست پکیج‌های رسمی کلینیک
  assert(OFFICIAL_CLINIC_PACKAGES.length === 5, 'وجود ۵ پکیج رسمی تعریف‌شده برای کلینیک');
  const hybridVip = OFFICIAL_CLINIC_PACKAGES.find(p => p.id === 'PKG_24_HYBRID_VIP');
  assert(hybridVip?.priceToman === 5200000 && hybridVip?.totalSessions === 24, 'صحت مشخصات پکیج ترکیبی VIP');

  console.log(`\n======================================================`);
  console.log(`نتیجه تست‌ها: ${passed} موفق | ${failed} ناموفق`);
  console.log(`======================================================\n`);

  if (failed > 0) {
    process.exit(1);
  }
}

runTests().catch((err) => {
  console.error('خطای غیرمنتظره در اجرای تست:', err);
  process.exit(1);
});
