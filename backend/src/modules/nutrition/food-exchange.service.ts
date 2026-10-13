export interface ExchangeResult {
  exchangeGroupFa: string;
  sourceFood: string;
  sourcePortionGrams: number;
  targetFood: string;
  targetPortionGrams: number;
  macroSummaryFa: string;
}

interface FoodExchangeRatio {
  nameFa: string;
  group: 'PROTEIN' | 'CARBOHYDRATE' | 'FAT' | 'VEGETABLES';
  baseEquivalentGrams: number; // وزن معادل ۱ واحد استاندارد
}

export class FoodExchangeService {
  private static foodDatabase: Record<string, FoodExchangeRatio> = {
    // گروه پروتئین (۱ واحد = حدود ۲۵-۳۰ گرم پروتئین خالص)
    'سینه مرغ پخته': { nameFa: 'سینه مرغ پخته', group: 'PROTEIN', baseEquivalentGrams: 150 },
    'ماهی قزل‌آلا': { nameFa: 'ماهی قزل‌آلا', group: 'PROTEIN', baseEquivalentGrams: 180 },
    'سفیده تخم‌مرغ': { nameFa: 'سفیده تخم‌مرغ', group: 'PROTEIN', baseEquivalentGrams: 140 }, // حدود ۴ عدد
    'کنسرو ماهی تن در آب‌نمک': { nameFa: 'کنسرو ماهی تن در آب‌نمک', group: 'PROTEIN', baseEquivalentGrams: 130 },
    'فیله گوساله بدون چربی': { nameFa: 'فیله گوساله بدون چربی', group: 'PROTEIN', baseEquivalentGrams: 140 },

    // گروه کربوهیدرات (۱ واحد = حدود ۳۰-۳۵ گرم کربوهیدرات پیچیده)
    'برنج کته قهوه‌ای یا سفید': { nameFa: 'برنج کته قهوه‌ای یا سفید', group: 'CARBOHYDRATE', baseEquivalentGrams: 150 },
    'جو دوسر پرک': { nameFa: 'جو دوسر پرک', group: 'CARBOHYDRATE', baseEquivalentGrams: 60 },
    'سیب‌زمینی آب‌پز': { nameFa: 'سیب‌زمینی آب‌پز', group: 'CARBOHYDRATE', baseEquivalentGrams: 200 },
    'نان سنگک سنتی': { nameFa: 'نان سنگک سنتی', group: 'CARBOHYDRATE', baseEquivalentGrams: 80 },
    'ماکارونی سبوس‌دار پخته': { nameFa: 'ماکارونی سبوس‌دار پخته', group: 'CARBOHYDRATE', baseEquivalentGrams: 140 },

    // گروه چربی‌های سالم (۱ واحد = حدود ۱۰-۱۲ گرم اسید چرب غیر اشباع)
    'روغن زیتون فرابکر': { nameFa: 'روغن زیتون فرابکر', group: 'FAT', baseEquivalentGrams: 12 },
    'بادام درختی خام': { nameFa: 'بادام درختی خام', group: 'FAT', baseEquivalentGrams: 20 },
    'کره بادام زمینی طبیعی': { nameFa: 'کره بادام زمینی طبیعی', group: 'FAT', baseEquivalentGrams: 18 },
    'گردو خام': { nameFa: 'گردو خام', group: 'FAT', baseEquivalentGrams: 15 },
  };

  /**
   * محاسبه مقدار گرم معادل جایگزینی ماده غذایی بدون برهم خوردن بالانس درشت‌مغذی‌ها
   */
  static calculateExchange(sourceFoodName: string, targetFoodName: string, sourcePortionGrams: number): ExchangeResult {
    const source = this.foodDatabase[sourceFoodName];
    const target = this.foodDatabase[targetFoodName];

    if (!source || !target) {
      throw new Error(`ماده غذایی یافت نشد. اقلام معتبر: ${Object.keys(this.foodDatabase).join('، ')}`);
    }

    if (source.group !== target.group) {
      throw new Error(`جایگزینی نامعتبر: [${source.nameFa}] از گروه ${source.group} نمی‌تواند با [${target.nameFa}] از گروه ${target.group} تعویض شود.`);
    }

    // محاسبه ضریب واحد مصرفی
    const units = sourcePortionGrams / source.baseEquivalentGrams;
    const targetPortionGrams = Math.round(units * target.baseEquivalentGrams);

    const groupNames: Record<string, string> = {
      PROTEIN: 'پروتئین خالص',
      CARBOHYDRATE: 'کربوهیدرات پیچیده',
      FAT: 'چربی‌های سالم',
      VEGETABLES: 'سبزیجات و فیبر',
    };

    return {
      exchangeGroupFa: groupNames[source.group] || source.group,
      sourceFood: source.nameFa,
      sourcePortionGrams,
      targetFood: target.nameFa,
      targetPortionGrams,
      macroSummaryFa: `${sourcePortionGrams} گرم ${source.nameFa} دقیقاً معادل ${targetPortionGrams} گرم ${target.nameFa} ارزش درشت‌مغذی دارد.`,
    };
  }
}
