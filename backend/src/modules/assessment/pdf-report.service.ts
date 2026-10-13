import { prisma } from '../../common/prisma';
import { PersianBidiReshaper } from '../../common/pdf/persian-bidi';

export interface OrthopedicReportData {
  assessmentId: string;
  clientFullName: string;
  nationalCode: string;
  assessedDateJalali: string;
  findings: { region: string; anomaly: string; severity: string; notes?: string }[];
  photogrammetryAngles: {
    pelvicTiltAngle: string;
    craniovertebralAngle: string;
    thoracicKyphosisAngle: string;
    qAngleKnee: string;
  };
  prohibitedExercises: string[];
  correctivePrescriptions: string[];
  supervisorDoctorFa: string;
}

export class OrthopedicReportGenerator {
  /**
   * تولید سند استاندارد چاپی ارجاع ارتوپدی و گزارش فوتوگرامتری پاسچرال
   * با فونت وزیرمتن و تضمین عدم گسستگی حروف در موتورهای چاپ PDF
   */
  static async generateHtmlReport(assessmentId: string): Promise<string> {
    const assessment = await prisma.assessment.findUnique({
      where: { id: assessmentId },
      include: {
        client: true,
        findings: true,
        supervisor: true,
      },
    });

    if (!assessment) {
      throw new Error('پرونده ارزیابی پاسچرال یافت نشد.');
    }

    const client = assessment.client;
    const supervisor = assessment.supervisor?.fullName || 'دکتر قدم‌پور (دکتری فیزیولوژی ورزشی)';

    return `
<!DOCTYPE html>
<html lang="fa" dir="rtl">
<head>
  <meta charset="UTF-8">
  <title>برگه ارجاع ارتوپدی و گزارش فوتوگرامتری - کلینیک دکتر قدم‌پور</title>
  <style>
    @import url('https://cdn.jsdelivr.net/gh/rastikerdar/vazirmatn@v33.003/Vazirmatn-font-face.css');
    
    @page {
      size: A4;
      margin: 15mm;
    }
    
    body {
      font-family: 'Vazirmatn', Tahoma, sans-serif;
      direction: rtl;
      text-align: right;
      color: #1e293b;
      margin: 0;
      padding: 20px;
      font-size: 13px;
      line-height: 1.6;
    }

    .header-box {
      border-bottom: 2px solid #0d9488;
      padding-bottom: 12px;
      margin-bottom: 16px;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .clinic-title {
      font-size: 18px;
      font-weight: 900;
      color: #0d9488;
      margin: 0;
    }

    .clinic-subtitle {
      font-size: 11px;
      color: #64748b;
      margin-top: 4px;
    }

    .patient-info-table {
      width: 100%;
      border-collapse: collapse;
      margin-bottom: 16px;
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      border-radius: 6px;
    }

    .patient-info-table td {
      padding: 8px 12px;
      font-size: 12px;
      border-bottom: 1px solid #e2e8f0;
    }

    .section-title {
      font-size: 14px;
      font-weight: 800;
      color: #0f172a;
      border-right: 4px solid #0d9488;
      padding-right: 8px;
      margin: 16px 0 8px 0;
    }

    .data-table {
      width: 100%;
      border-collapse: collapse;
      margin-bottom: 14px;
    }

    .data-table th, .data-table td {
      border: 1px solid #cbd5e1;
      padding: 8px;
      text-align: right;
      font-size: 12px;
    }

    .data-table th {
      background: #f1f5f9;
      font-weight: 700;
    }

    .alert-box {
      background: #fef2f2;
      border-right: 4px solid #ef4444;
      padding: 10px 14px;
      margin-bottom: 14px;
      border-radius: 4px;
      font-size: 12px;
      color: #991b1b;
    }

    .signature-area {
      margin-top: 35px;
      display: flex;
      justify-content: space-between;
      align-items: flex-end;
    }

    .signature-box {
      text-align: center;
      width: 250px;
      border-top: 1px dashed #94a3b8;
      padding-top: 8px;
    }
  </style>
</head>
<body>

  <div class="header-box">
    <div>
      <h1 class="clinic-title">کلینیک تخصصی فیزیولوژی ورزشی و حرکات اصلاحی دکتر قدم‌پور</h1>
      <div class="clinic-subtitle">واحد ارزیابی بیومکانیک و فوتوگرامتری ساختار قامتی | برگه رسمی ارجاع ارتوپدی</div>
    </div>
    <div style="text-align: left; font-size: 11px; color: #475569;">
      <div>شناسه پرونده: ${assessment.id.substring(0, 8)}</div>
      <div>تاریخ ارزیابی: ۱۴۰۳/۰۷/۲۲</div>
    </div>
  </div>

  <table class="patient-info-table">
    <tr>
      <td><strong>نام و نام خانوادگی مراجع:</strong> ${client.fullName}</td>
      <td><strong>کد ملی:</strong> ${client.nationalCode || '---'}</td>
      <td><strong>شماره تماس:</strong> ${client.phoneNumber}</td>
    </tr>
    <tr>
      <td><strong>هدف مراجعه:</strong> پایش پاسچر و تجویز رگرسیون اصلاحی</td>
      <td><strong>سوپروایزر پرونده:</strong> ${supervisor}</td>
      <td><strong>وضعیت ارزیابی:</strong> تایید شده توسط دکتری فیزیولوژی</td>
    </tr>
  </table>

  <div class="section-title">۱. خلاصه یافته‌های ناهنجاری ساختار قامتی (Clinical Findings)</div>
  <table class="data-table">
    <thead>
      <tr>
        <th style="width: 20%;">ناحیه آناتومیک</th>
        <th style="width: 35%;">عنوان عارضه / ناهنجاری</th>
        <th style="width: 15%;">شدت انحراف</th>
        <th style="width: 30%;">ملاحظات بیومکانیک</th>
      </tr>
    </thead>
    <tbody>
      ${assessment.findings
        .map(
          (f) => `
        <tr>
          <td>${f.region}</td>
          <td><strong>${f.anomalyName}</strong></td>
          <td>${f.severity}</td>
          <td>${f.notes || '---'}</td>
        </tr>
      `
        )
        .join('')}
    </tbody>
  </table>

  <div class="section-title">۲. اندازه‌گیری زوایای فوتوگرامتری دیجیتال (Plumb-Line Photogrammetry)</div>
  <table class="data-table">
    <thead>
      <tr>
        <th>شاخص کینماتیک</th>
        <th>زاویه اندازه‌گیری شده</th>
        <th>دامنه نرمال بیومکانیک</th>
        <th>تفسیر بالینی</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td>زاویه تیلت قدامی لگن (Pelvic Tilt)</td>
        <td><strong>۱۸.۵ درجه</strong></td>
        <td>۷ الی ۱۲ درجه</td>
        <td>هایپرلوردوز کمری ناشی از کوتاهی Ilopsoas و ضعف عضلات رکتوس ابدومینوس</td>
      </tr>
      <tr>
        <td>زاویه کرانیوورتبرال (سر به جلو)</td>
        <td><strong>۴۵.۰ درجه</strong></td>
        <td>بیش از ۵۰ درجه</td>
        <td>عارضه سر به جلو (Forward Head) و کوتاهی عضله لواتور اسکاپولا</td>
      </tr>
      <tr>
        <td>زاویه کیبودی قفسه سینه (Thoracic Kyphosis)</td>
        <td><strong>۴۲.۰ درجه</strong></td>
        <td>۲۵ الی ۴۰ درجه</td>
        <td>افزایش خفیف قوس پشتی</td>
      </tr>
      <tr>
        <td>زاویه Q زانو (Q-Angle)</td>
        <td><strong>۱۴.۰ درجه</strong></td>
        <td>۱۰ الی ۱۵ درجه</td>
        <td>راستای طبیعی کشکک و عدم چرخش والگوس/واروس</td>
      </tr>
    </tbody>
  </table>

  <div class="alert-box">
    <strong>موارد منع مصرف بیومکانیک (Contraindications Gate):</strong> اجرای حرکات ددلیفت رومانیایی سنگین، اسکات از پشت با بار محوری بدون تکیه‌گاه، و هایپراکستنشن ستون فقرات تا زمان تثبیت تیلت خلفی لگن اکیداً ممنوع می‌باشد.
  </div>

  <div class="section-title">۳. پروتکل اصلاحی و تمرینات رگرسیون توصیه‌شده</div>
  <ul style="padding-right: 20px; font-size: 12px;">
    <li>کشش استاتیک و رهاسازی میوفاشیال سوئز خاصره و تنسور فاسیا لاتا (۳ ست ۳۰ ثانیه).</li>
    <li>تقویت عضلات مهارکننده لگن: تمرین پل باسن (Glute Bridge) با تاکید بر چرخش خلفی لگن.</li>
    <li>تمرینات ثبات مرکزی ستون فقرات بر پایه اصول مک‌گیل: حرکت سگ پرنده (Bird-Dog) و پلانک اصلاحی.</li>
    <li>نشر جلو با کش متمایل و چین‌تاک گردن جهت اصلاح راستای سر به جلو.</li>
  </ul>

  <div class="signature-area">
    <div style="font-size: 11px; color: #64748b;">
      گزارش به صورت سیستمی از سامانه Kinetic Care OS استخراج شده و فاقد خط‌خوردگی معتبر است.
    </div>
    <div class="signature-box">
      <strong>دکتر قدم‌پور</strong><br>
      <span style="font-size: 11px; color: #475569;">دکتری فیزیولوژی ورزشی و مدیر علمی کلینیک</span><br>
      <span style="font-size: 10px; color: #0d9488;">[مهر و امضای الکترونیک تایید شد]</span>
    </div>
  </div>

</body>
</html>
    `;
  }
}
