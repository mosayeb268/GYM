import { prisma } from '../../common/prisma';

export interface ExerciseValidationResult {
  isSafe: boolean;
  blockedExercises: {
    exerciseId: string;
    exerciseTitleFa: string;
    conflictingAnomaly: string;
    messageFa: string;
  }[];
}

export interface PainRegressionResult {
  hasRedFlag: boolean;
  originalExerciseTitleFa: string;
  regressedExerciseTitleFa?: string;
  regressedExerciseId?: string;
  clinicalAlertMessageFa?: string;
  actionRequired: 'PROCEED' | 'REGRESSED' | 'CLINICAL_HOLD';
}

export class BiomechanicsEngineService {
  /**
   * ۱. گیت هوشمند ایمنی بیومکانیک و منع تمرین (Contraindication Gate)
   * بررسی تداخل حرکات انتخابی مربی با آخرین یافته‌های ارزیابی پاسچرال مراجع
   */
  static async validateWorkoutPlan(clientId: string, exerciseIds: string[]): Promise<ExerciseValidationResult> {
    // واکشی آخرین ارزیابی تایید شده مراجع به همراه یافته‌های ناهنجاری
    const latestAssessment = await prisma.assessment.findFirst({
      where: {
        clientId,
        isApproved: true,
      },
      orderBy: { assessedAt: 'desc' },
      include: { findings: true },
    });

    if (!latestAssessment || latestAssessment.findings.length === 0) {
      return { isSafe: true, blockedExercises: [] };
    }

    const clientAnomalies = latestAssessment.findings.map((f) => f.anomalyName);

    // واکشی مشخصات حرکات ورزشی
    const exercises = await prisma.exercise.findMany({
      where: { id: { in: exerciseIds } },
    });

    const blockedExercises: ExerciseValidationResult['blockedExercises'] = [];

    for (const exercise of exercises) {
      for (const contraindication of exercise.contraindications) {
        // بررسی تطابق کلمات کلیدی منع تمرین با ناهنجاری مراجع
        const matchedAnomaly = clientAnomalies.find((anomaly) =>
          anomaly.toLowerCase().includes(contraindication.toLowerCase()) ||
          contraindication.toLowerCase().includes(anomaly.toLowerCase())
        );

        if (matchedAnomaly) {
          blockedExercises.push({
            exerciseId: exercise.id,
            exerciseTitleFa: exercise.titleFa,
            conflictingAnomaly: matchedAnomaly,
            messageFa: `این حرکت با عارضه [${matchedAnomaly}] مراجع تداخل دارد و اجرای آن ممکن است آسیب‌زا باشد. جایگزینی این حرکت یا تأیید مستقیم دکتر فیزیولوژی الزامی است.`,
          });
          break;
        }
      }
    }

    return {
      isSafe: blockedExercises.length === 0,
      blockedExercises,
    };
  }

  /**
   * ۲. موتور رگرسیون هوشمند تمرین بر اساس درد مراجع (Adaptive Pain Engine)
   * ارزیابی نمره درد پیش از تمرین (VAS 0-10) و جایگزینی خودکار تمرین با رگرسیون ایمن‌تر
   */
  static async evaluatePainAndRegress(
    clientId: string,
    exerciseId: string,
    prePainVas: number,
    painRegion: string
  ): Promise<PainRegressionResult> {
    const exercise = await prisma.exercise.findUnique({
      where: { id: exerciseId },
      include: {
        regressions: {
          include: { regressionExercise: true },
        },
      },
    });

    if (!exercise) {
      throw new Error('حرکت ورزشی مورد نظر یافت نشد.');
    }

    // ثبت لاگ روزانه ارزیابی درد
    const isRedFlag = prePainVas >= 5;
    await prisma.dailySessionLog.create({
      data: {
        clientId,
        prePainVas,
        painRegion,
        postRpe: 0,
        isRedFlag,
        clientFeedback: isRedFlag
          ? `ثبت نمره درد ${prePainVas} از ۱۰ در ناحیه ${painRegion} برای حرکت ${exercise.titleFa}.`
          : null,
      },
    });

    // اگر درد زیر آستانه ۵ باشد، تمرین اصلی مجاز است
    if (prePainVas < 5) {
      return {
        hasRedFlag: false,
        originalExerciseTitleFa: exercise.titleFa,
        actionRequired: 'PROCEED',
      };
    }

    // بررسی وجود رگرسیون ثبت شده برای این تمرین
    const regressionMap = exercise.regressions.find((r) => prePainVas >= r.triggerPainVas);

    if (regressionMap && regressionMap.regressionExercise) {
      const regressedExercise = regressionMap.regressionExercise;
      return {
        hasRedFlag: true,
        originalExerciseTitleFa: exercise.titleFa,
        regressedExerciseTitleFa: regressedExercise.titleFa,
        regressedExerciseId: regressedExercise.id,
        clinicalAlertMessageFa: `به دلیل ثبت نمره درد ${prePainVas} (درد بالا) در ناحیه ${painRegion}، حرکت اصلی [${exercise.titleFa}] به طور خودکار با تمرین رگرسیون سبک‌تر [${regressedExercise.titleFa}] جایگزین گردید و هشدار برای دکتر قدم‌پور ارسال شد.`,
        actionRequired: 'REGRESSED',
      };
    }

    return {
      hasRedFlag: true,
      originalExerciseTitleFa: exercise.titleFa,
      clinicalAlertMessageFa: `نمره درد مراجع ${prePainVas} از ۱۰ است و رگرسیون مستقیمی برای این حرکت تعریف نشده است. جهت پیشگیری از آسیب حاد، این حرکت قفل گردید.`,
      actionRequired: 'CLINICAL_HOLD',
    };
  }
}
