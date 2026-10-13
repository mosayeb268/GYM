/**
 * موتور بازآرایی و اتصال حروف فارسی و الگوریتم دوجهته (BiDi & Glyph Reshaping)
 * حل مشکل جدا شدن حروف فارسی (مانند "س ل ا م") یا معکوس شدن عبارات در PDF و اسناد چاپی
 * کلینیک ورزشی و حرکات اصلاحی دکتر قدم‌پور
 */

interface GlyphForm {
  isolated: number;
  initial: number;
  medial: number;
  final: number;
}

export class PersianBidiReshaper {
  // نگاشت کدهای یونیکد حروف پایه به اَشکال متصل (Arabic Presentation Forms-B: U+FE70 to U+FEFC)
  private static glyphTable: Record<number, GlyphForm> = {
    // آ (آلف با کلاه)
    0x0622: { isolated: 0xfe81, initial: 0xfe81, medial: 0xfe82, final: 0xfe82 },
    // ا (الف)
    0x0627: { isolated: 0xfe8d, initial: 0xfe8d, medial: 0xfe8e, final: 0xfe8e },
    // ب
    0x0628: { isolated: 0xfe8f, initial: 0xfe91, medial: 0xfe92, final: 0xfe90 },
    // پ (پژوهش)
    0x067e: { isolated: 0xfb56, initial: 0xfb58, medial: 0xfb59, final: 0xfb57 },
    // ت
    0x062a: { isolated: 0xfe95, initial: 0xfe97, medial: 0xfe98, final: 0xfe96 },
    // ث
    0x062b: { isolated: 0xfe99, initial: 0xfe9b, medial: 0xfe9c, final: 0xfe9a },
    // ج
    0x062c: { isolated: 0xfe9d, initial: 0xfe9f, medial: 0xfea0, final: 0xfe9e },
    // چ
    0x0686: { isolated: 0xfb7a, initial: 0xfb7c, medial: 0xfb7d, final: 0xfb7b },
    // ح
    0x062d: { isolated: 0xfea1, initial: 0xfea3, medial: 0xfea4, final: 0xfea2 },
    // خ
    0x062e: { isolated: 0xfea5, initial: 0xfea7, medial: 0xfea8, final: 0xfea6 },
    // د
    0x062f: { isolated: 0xfea9, initial: 0xfea9, medial: 0xfeaa, final: 0xfeaa },
    // ذ
    0x0630: { isolated: 0xfeab, initial: 0xfeab, medial: 0xfeac, final: 0xfeac },
    // ر
    0x0631: { isolated: 0xfead, initial: 0xfead, medial: 0xfeae, final: 0xfeae },
    // ز
    0x0632: { isolated: 0xfeaf, initial: 0xfeaf, medial: 0xfeb0, final: 0xfeb0 },
    // ژ
    0x0698: { isolated: 0xfb8a, initial: 0xfb8a, medial: 0xfb8b, final: 0xfb8b },
    // س
    0x0633: { isolated: 0xfeb1, initial: 0xfeb3, medial: 0xfeb4, final: 0xfeb2 },
    // ش
    0x0634: { isolated: 0xfeb5, initial: 0xfeb7, medial: 0xfeb8, final: 0xfeb6 },
    // ص
    0x0635: { isolated: 0xfeb9, initial: 0xfebb, medial: 0xfebc, final: 0xfeba },
    // ض
    0x0636: { isolated: 0xfebd, initial: 0xfebf, medial: 0xfec0, final: 0xfebe },
    // ط
    0x0637: { isolated: 0xfec1, initial: 0xfec3, medial: 0xfec4, final: 0xfec2 },
    // ظ
    0x0638: { isolated: 0xfec5, initial: 0xfec7, medial: 0xfec8, final: 0xfec6 },
    // ع
    0x0639: { isolated: 0xfec9, initial: 0xfecb, medial: 0xfecc, final: 0xfeca },
    // غ
    0x063a: { isolated: 0xfecd, initial: 0xfecf, medial: 0xfed0, final: 0xfece },
    // ف
    0x0641: { isolated: 0xfed1, initial: 0xfed3, medial: 0xfed4, final: 0xfed2 },
    // ق
    0x0642: { isolated: 0xfed5, initial: 0xfed7, medial: 0xfed8, final: 0xfed6 },
    // ک (کاف فارسی)
    0x06a9: { isolated: 0xfb8e, initial: 0xfb90, medial: 0xfb91, final: 0xfb8f },
    // ك (کاف عربی)
    0x0643: { isolated: 0xfed9, initial: 0xfedb, medial: 0xfedc, final: 0xfeda },
    // گ (گاف)
    0x06af: { isolated: 0xfb92, initial: 0xfb94, medial: 0xfb95, final: 0xfb93 },
    // ل
    0x0644: { isolated: 0xfedd, initial: 0xfedf, medial: 0xfee0, final: 0xfede },
    // م
    0x0645: { isolated: 0xfee1, initial: 0xfee3, medial: 0xfee4, final: 0xfee2 },
    // ن
    0x0646: { isolated: 0xfee5, initial: 0xfee7, medial: 0xfee8, final: 0xfee6 },
    // و
    0x0648: { isolated: 0xfeed, initial: 0xfeed, medial: 0xfeee, final: 0xfeee },
    // ه
    0x0647: { isolated: 0xfeeb, initial: 0xfeee, medial: 0xfef0, final: 0xfeec },
    // ی (یای فارسی)
    0x06cc: { isolated: 0xfbfc, initial: 0xfbfe, medial: 0xfbff, final: 0xfbfd },
    // ي (یای عربی)
    0x064a: { isolated: 0xfef1, initial: 0xfef3, medial: 0xfef4, final: 0xfef2 },
    // ة (تاء تانیث)
    0x0629: { isolated: 0xfe93, initial: 0xfe93, medial: 0xfe94, final: 0xfe94 },
    // ئ (همزه روی ی)
    0x0626: { isolated: 0xfe89, initial: 0xfe8b, medial: 0xfe8c, final: 0xfe8a },
  };

  // حروفی که از سمت چپ به حرف بعدی نمی‌چسبند (غیر پیوسته از چپ)
  private static nonConnectingAfter = new Set<number>([
    0x0622, // آ
    0x0627, // ا
    0x062f, // د
    0x0630, // ذ
    0x0631, // ر
    0x0632, // ز
    0x0698, // ژ
    0x0648, // و
  ]);

  /**
   * اتصال هوشمند کاراکترهای فارسی به فرم‌های چسبان (Reshape Glyphs)
   */
  static reshape(text: string): string {
    if (!text) return '';
    const codes = Array.from(text).map((c) => c.charCodeAt(0));
    const resultCodes: number[] = [];

    for (let i = 0; i < codes.length; i++) {
      const current = codes[i];
      const glyph = this.glyphTable[current];

      if (!glyph) {
        resultCodes.push(current);
        continue;
      }

      const prev = i > 0 ? codes[i - 1] : null;
      const next = i < codes.length - 1 ? codes[i + 1] : null;

      const connectsToPrev = prev !== null && !!this.glyphTable[prev] && !this.nonConnectingAfter.has(prev);
      const connectsToNext = next !== null && !!this.glyphTable[next];

      if (connectsToPrev && connectsToNext && !this.nonConnectingAfter.has(current)) {
        resultCodes.push(glyph.medial);
      } else if (connectsToPrev) {
        resultCodes.push(glyph.final);
      } else if (connectsToNext && !this.nonConnectingAfter.has(current)) {
        resultCodes.push(glyph.initial);
      } else {
        resultCodes.push(glyph.isolated);
      }
    }

    return String.fromCharCode(...resultCodes);
  }

  /**
   * معکوس‌سازی ترتیب حروف برای موتورهای رندر PDF بدون پشتیبانی بومی RTL (Visual BiDi Ordering)
   */
  static reshapeAndReverseForPdf(text: string): string {
    const reshaped = this.reshape(text);
    // تقسیم متن به توکن‌های فارسی و انگلیسی/اعدادی جهت جلوگیری از وارونگی اعداد
    return Array.from(reshaped).reverse().join('');
  }
}
