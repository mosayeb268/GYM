import express, { Request, Response } from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import { OtpService } from './modules/auth/otp.service';
import { QrTurnstileService } from './modules/access/qr.service';
import { BiomechanicsEngineService } from './modules/biomechanics/biomechanics.service';
import { FoodExchangeService } from './modules/nutrition/food-exchange.service';
import { ZarinPalPaymentService } from './modules/payment/zarinpal.service';
import { OrthopedicReportGenerator } from './modules/assessment/pdf-report.service';

dotenv.config();

const app = express();
const PORT = process.env.PORT || 4000;

app.use(cors());
app.use(express.json());

// مسیر وضعیت سرور
app.get('/api/v1/health', (_req: Request, res: Response) => {
  res.json({
    status: 'ONLINE',
    system: 'Kinetic Care OS & Gym ERP',
    clinic: 'کلینیک ورزشی و حرکات اصلاحی دکتر قدم‌پور',
    version: '1.0.0',
    timestampUtc: new Date().toISOString(),
  });
});

// ۱. ماژول احراز هویت پیامکی (SMS OTP)
app.post('/api/v1/auth/request-otp', async (req: Request, res: Response) => {
  try {
    const { phoneNumber } = req.body;
    if (!phoneNumber) {
      return res.status(400).json({ error: 'شماره تلفن همراه الزامی است.' });
    }
    const result = await OtpService.requestOtp(phoneNumber);
    return res.json(result);
  } catch (error: any) {
    return res.status(400).json({ error: error.message });
  }
});

app.post('/api/v1/auth/verify-otp', async (req: Request, res: Response) => {
  try {
    const { phoneNumber, code } = req.body;
    if (!phoneNumber || !code) {
      return res.status(400).json({ error: 'شماره تلفن همراه و کد تایید الزامی است.' });
    }
    const result = await OtpService.verifyOtp(phoneNumber, code);
    return res.json(result);
  } catch (error: any) {
    return res.status(400).json({ error: error.message });
  }
});

// ۲. ماژول تردد و بارکد پویای گیت (Dynamic QR Turnstile)
app.post('/api/v1/access/generate-pass', (req: Request, res: Response) => {
  try {
    const { userId } = req.body;
    if (!userId) {
      return res.status(400).json({ error: 'شناسه کاربر الزامی است.' });
    }
    const pass = QrTurnstileService.generateDynamicPass(userId);
    return res.json(pass);
  } catch (error: any) {
    return res.status(400).json({ error: error.message });
  }
});

app.post('/api/v1/access/verify-qr', async (req: Request, res: Response) => {
  try {
    const { qrData, gateDeviceId } = req.body;
    if (!qrData) {
      return res.status(400).json({ error: 'اطلاعات بارکد پویا الزامی است.' });
    }
    const result = await QrTurnstileService.verifyTurnstilePass(qrData, gateDeviceId);
    return res.json(result);
  } catch (error: any) {
    return res.status(400).json({ error: error.message });
  }
});

// ۳. ماژول قوانین بیومکانیک و رگرسیون درد
app.post('/api/v1/biomechanics/validate-workout', async (req: Request, res: Response) => {
  try {
    const { clientId, exerciseIds } = req.body;
    if (!clientId || !Array.isArray(exerciseIds)) {
      return res.status(400).json({ error: 'شناسه مراجع و لیست تمرینات الزامی است.' });
    }
    const validation = await BiomechanicsEngineService.validateWorkoutPlan(clientId, exerciseIds);
    return res.json(validation);
  } catch (error: any) {
    return res.status(400).json({ error: error.message });
  }
});

app.post('/api/v1/biomechanics/evaluate-pain', async (req: Request, res: Response) => {
  try {
    const { clientId, exerciseId, prePainVas, painRegion } = req.body;
    if (!clientId || !exerciseId || prePainVas === undefined) {
      return res.status(400).json({ error: 'اطلاعات ارزیابی درد ناقص است.' });
    }
    const regression = await BiomechanicsEngineService.evaluatePainAndRegress(
      clientId,
      exerciseId,
      prePainVas,
      painRegion || 'نامشخص'
    );
    return res.json(regression);
  } catch (error: any) {
    return res.status(400).json({ error: error.message });
  }
});

// ۴. ماژول جایگزینی واحدهای غذایی
app.post('/api/v1/nutrition/exchange', (req: Request, res: Response) => {
  try {
    const { sourceFood, targetFood, portionGrams } = req.body;
    if (!sourceFood || !targetFood || !portionGrams) {
      return res.status(400).json({ error: 'مشخصات ماده غذایی مبدا، مقصد و مقدار گرم الزامی است.' });
    }
    const result = FoodExchangeService.calculateExchange(sourceFood, targetFood, portionGrams);
    return res.json(result);
  } catch (error: any) {
    return res.status(400).json({ error: error.message });
  }
});

// ۵. ماژول پرداخت آنلاین شهریه و اشتراک به تومان (ZarinPal / Shaparak)
app.post('/api/v1/payment/checkout', async (req: Request, res: Response) => {
  try {
    const { userId, subscriptionId, amountToman, callbackUrl } = req.body;
    if (!userId || !subscriptionId || !amountToman) {
      return res.status(400).json({ error: 'اطلاعات کاربر، اشتراک و مبلغ الزامی است.' });
    }
    const result = await ZarinPalPaymentService.requestPayment(
      userId,
      subscriptionId,
      amountToman,
      callbackUrl || 'http://localhost:3000/payment/callback'
    );
    return res.json(result);
  } catch (error: any) {
    return res.status(400).json({ error: error.message });
  }
});

app.post('/api/v1/payment/verify', async (req: Request, res: Response) => {
  try {
    const { authority, subscriptionId, amountToman } = req.body;
    if (!authority || !subscriptionId || !amountToman) {
      return res.status(400).json({ error: 'شناسه مرجع، شناسه اشتراک و مبلغ الزامی است.' });
    }
    const result = await ZarinPalPaymentService.verifyPayment(authority, subscriptionId, amountToman);
    return res.json(result);
  } catch (error: any) {
    return res.status(400).json({ error: error.message });
  }
});

// ۶. تولید و دانلود گزارش ارجاع ارتوپدی و فوتوگرامتری مراجع
app.get('/api/v1/assessment/:id/pdf-report', async (req: Request, res: Response) => {
  try {
    const { id } = req.params;
    const htmlReport = await OrthopedicReportGenerator.generateHtmlReport(id);
    res.setHeader('Content-Type', 'text/html; charset=utf-8');
    return res.send(htmlReport);
  } catch (error: any) {
    return res.status(404).json({ error: error.message });
  }
});

app.listen(PORT, () => {
  console.log(`================================================================`);
  console.log(`سرور سامانه کینتیک کر (Kinetic Care OS) با موفقیت راه‌اندازی شد.`);
  console.log(`کلینیک ورزشی و حرکات اصلاحی دکتر قدم‌پور`);
  console.log(`پورت دسترسی: http://localhost:${PORT}`);
  console.log(`================================================================`);
});
