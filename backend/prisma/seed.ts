import { PrismaClient, Role, SubscriptionType, SubscriptionStatus, PosturalView } from '@prisma/client';

const prisma = new PrismaClient();

async function main() {
  console.log('--- شروع بذرپاشی داده‌های پایه کلینیک ورزشی و حرکات اصلاحی دکتر قدم‌پور ---');

  // ۱. ایجاد کاربر مدیر علمی و سوپروایزر (دکتر قدم‌پور)
  const supervisor = await prisma.user.upsert({
    where: { phoneNumber: '09120000001' },
    update: {},
    create: {
      phoneNumber: '09120000001',
      nationalCode: '0011223344',
      fullName: 'دکتر قدم‌پور (دکتری فیزیولوژی ورزشی)',
      role: Role.CLINICAL_DIRECTOR,
      isActive: true,
    },
  });

  // ۲. ایجاد کارشناس حرکات اصلاحی
  const trainer = await prisma.user.upsert({
    where: { phoneNumber: '09120000002' },
    update: {},
    create: {
      phoneNumber: '09120000002',
      nationalCode: '0022334455',
      fullName: 'استاد علیرضا مرادی (متخصص حرکات اصلاحی)',
      role: Role.CORRECTIVE_TRAINER,
      isActive: true,
    },
  });

  // ۳. ایجاد مراجع / ورزشکار
  const client = await prisma.user.upsert({
    where: { phoneNumber: '09123456789' },
    update: {},
    create: {
      phoneNumber: '09123456789',
      nationalCode: '0033445566',
      fullName: 'سارا احمدی',
      role: Role.ATHLETE_CLIENT,
      isActive: true,
    },
  });

  // ۴. ایجاد اشتراک فعال به تومان
  const sub = await prisma.subscription.create({
    data: {
      userId: client.id,
      planTitle: 'پکیج ترکیبی VIP: اصلاحی بالینی + هایپرتروفی',
      type: SubscriptionType.HYBRID,
      status: SubscriptionStatus.ACTIVE,
      startDate: new Date(),
      endDate: new Date(Date.now() + 60 * 24 * 60 * 60 * 1000), // ۶۰ روز بعد
      totalSessions: 24,
      remainingSessions: 18,
      includesClinical: true,
      priceToman: 3850000,
    },
  });

  // ۵. ایجاد ارزیابی پاسچرال با ناهنجاری هایپرلوردوز کمری
  const assessment = await prisma.assessment.create({
    data: {
      clientId: client.id,
      evaluatorId: trainer.id,
      supervisorId: supervisor.id,
      isApproved: true,
      notesFa: 'در نمای لترال، زاویه تیلت قدامی لگن بیش از ۱۵ درجه و لوردوز کمری شدید مشاهده شد.',
      findings: {
        create: [
          {
            region: 'کمر و لگن',
            anomalyName: 'هایپرلوردوز کمری (گودی کمر بیش‌ازحد)',
            severity: 'متوسط تا شدید',
            notes: 'کوتاهی سوئز خاصره و ضعف عضلات شکم و گلوتئال',
          },
          {
            region: 'گردن',
            anomalyName: 'عارضه سر به جلو',
            severity: 'خفیف',
            notes: 'شیب زاویه کرانیوورتبرال ۴۶ درجه',
          },
        ],
      },
    },
  });

  // ۶. ایجاد حرکات ورزشی و قوانین رگرسیون تداخل بیومکانیک
  const rdl = await prisma.exercise.create({
    data: {
      titleFa: 'ددلیفت رومانیایی با هالتر (RDL)',
      muscleGroup: 'همسترینگ و سرینی',
      cuesFa: 'ستون فقرات کاملاً خنثی، لولا شدن از مفصل ران، عدم چرخش قدامی لگن',
      contraindications: ['هایپرلوردوز', 'گودی کمر بیش‌ازحد', 'فتق دیسک کمری'],
    },
  });

  const gluteBridge = await prisma.exercise.create({
    data: {
      titleFa: 'پل باسن روی زمین با تمرکز بر رتروورژن لگن (Glute Bridge)',
      muscleGroup: 'سرینی بزرگ',
      cuesFa: 'فشردن شکم به زمین، فعال‌سازی باسن در نقطه اوج بدون هایپراکستنشن کمر',
      contraindications: [],
    },
  });

  const squat = await prisma.exercise.create({
    data: {
      titleFa: 'اسکات از پشت با هالتر (Barbell Back Squat)',
      muscleGroup: 'چهارسر ران و سرینی',
      cuesFa: 'کمر صاف، زانوها در راستای شست پا، عمق استاندارد موازی با زمین',
      contraindications: ['هایپرلوردوز', 'زانوی ضربدری شدید'],
    },
  });

  const boxSquat = await prisma.exercise.create({
    data: {
      titleFa: 'اسکات به جعبه با دمبل (Goblet Box Squat)',
      muscleGroup: 'چهارسر ران',
      cuesFa: 'نشستن کنترل شده روی جعبه، حفظ پایداری عضلات کور',
      contraindications: [],
    },
  });

  // ثبت رگرسیون: اگر ددلیفت درد ایجاد کرد یا ناهنجاری بود -> تبدیل به پل باسن
  await prisma.exerciseRegression.create({
    data: {
      primaryExerciseId: rdl.id,
      regressionExerciseId: gluteBridge.id,
      triggerPainVas: 5,
    },
  });

  await prisma.exerciseRegression.create({
    data: {
      primaryExerciseId: squat.id,
      regressionExerciseId: boxSquat.id,
      triggerPainVas: 5,
    },
  });

  console.log('--- داده‌های پایه با موفقیت ثبت شدند ---');
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
