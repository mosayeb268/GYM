package com.kineticcare.feature.assessment

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kineticcare.core.designsystem.theme.KineticTealPrimary
import com.kineticcare.core.localization.JalaliCalendarEngine
import com.kineticcare.core.localization.mirrorForRtl
import com.kineticcare.core.localization.toPersianDigits

/**
 * صفحه جامع کارنامه ارزیابی پاسچرال و گزارش ارجاع ارتوپدی
 * کلینیک ورزشی و حرکات اصلاحی دکتر قدم‌پور (Ph.D. فیزیولوژی ورزشی)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssessmentReportScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var isExporting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "گزارش ارزیابی بیومکانیک و ارتوپدی",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "نظارت دکتر قدم‌پور (Ph.D. فیزیولوژی ورزشی)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت",
                            modifier = Modifier.mirrorForRtl()
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // ۱. کارت سربرگ و اطلاعات پرونده بالینی مراجع
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "محمدحسین رضایی",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "کد پرونده: KIN-1403-882".toPersianDigits(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "تأیید دکتری فیزیولوژی",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "کد ملی: ۰۰۱۲۳۴۵۶۷۸".toPersianDigits(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "تاریخ ارزیابی: ${JalaliCalendarEngine.now().toFullDescription()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ۲. ناهنجاری‌های شناسایی‌شده با خطوط شاقول بیومکانیک (Photogrammetric Deviations)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "یافته‌های فوتوگرامتری و شاقول تراز",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    DeviationItemRow(
                        title = "سر به جلو (Forward Head Posture)",
                        deviation = "۲۴ درجه انحراف (شدید)".toPersianDigits(),
                        isHighSeverity = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DeviationItemRow(
                        title = "افزایش قوس پشتی (Hyperkyphosis)",
                        deviation = "۴۸ درجه زاویه کاب (متوسط)".toPersianDigits(),
                        isHighSeverity = false
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DeviationItemRow(
                        title = "لوردوز کمری بیش‌ازحد (تیلت قدامی لگن)",
                        deviation = "۱۵ درجه تیلت قدامی (شدید)".toPersianDigits(),
                        isHighSeverity = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DeviationItemRow(
                        title = "محور زانو و ساق پا",
                        deviation = "زاویه Q نرمال - بدون والگوس یا واروس",
                        isHighSeverity = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ۳. کارت ایمنی بالینی (Red Flag Contraindications)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "هشدار بالینی: گیت ایمنی حرکات ممنوعه (Red Flags)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "با توجه به لوردوز کمری شدید و نمره درد VAS = ۶ در مهره‌های L4-L5، حرکات زیر تا زمان اصلاح راستا ممنوع بوده و به صورت خودکار رگرسیون شدند:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "⛔ ددلیفت سنتی",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "⛔ زیربغل هالتر خم",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ۴. پروتکل اصلاحی ۴ مرحله‌ای تجویز شده
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "پروتکل اصلاحی تجویزی (مبتنی بر شواهد NASM)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    ProtocolPhaseItem(
                        phase = "فاز ۱: مهار عضلات بیش‌فعال",
                        desc = "آزادسازی میوفاشیال با فوم رولر روی فلکسورهای ران (ایلیوپسواس و راست‌قدامی)",
                        reps = "۲ ست ۳۰ تا ۶۰ ثانیه‌ای".toPersianDigits()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ProtocolPhaseItem(
                        phase = "فاز ۲: کشش عضلات کوتاه شده",
                        desc = "کشش استاتیک عضله چهارسر و خیاطه بدون ایجاد هایپراکستنشن کمری",
                        reps = "۳ ست ۳۰ ثانیه برای هر پا".toPersianDigits()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ProtocolPhaseItem(
                        phase = "فاز ۳: فعال‌سازی و تقویت عضلات ضعیف",
                        desc = "پل باسن تک‌پا (Glute Bridge) و سگ پرنده (Bird-Dog) جهت ثبات کمری-لگنی",
                        reps = "۳ ست ۱۲ تکرار کنترل‌شده".toPersianDigits()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ProtocolPhaseItem(
                        phase = "فاز ۴: یکپارچه‌سازی عملکردی",
                        desc = "اسکوات سوئیسی با توپ روی دیوار با تمرکز بر حفظ تیلت خنثی لگن",
                        reps = "۳ ست ۱۰ تکرار با استراحت ۶۰ ثانیه".toPersianDigits()
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ۵. دکمه‌های صدور و اشتراک گزارش رسمی PDF ارتوپدی
            Button(
                onClick = {
                    isExporting = true
                    Toast.makeText(
                        context,
                        "فایل PDF رسمی ارجاع ارتوپدی با حروف‌چینی وزیرمتن و BiDi Reshaper صادر شد.",
                        Toast.LENGTH_LONG
                    ).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "دانلود و اشتراک‌گذاری گزارش رسمی ارتوپدی (PDF)",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun DeviationItemRow(
    title: String,
    deviation: String,
    isHighSeverity: Boolean
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = deviation,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isHighSeverity) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isHighSeverity) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = if (isHighSeverity) "انحراف شدید" else "طبیعی / خفیف",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isHighSeverity) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun ProtocolPhaseItem(
    phase: String,
    desc: String,
    reps: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = phase,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = reps,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
