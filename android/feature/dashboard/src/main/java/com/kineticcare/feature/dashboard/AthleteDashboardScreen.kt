package com.kineticcare.feature.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kineticcare.core.designsystem.theme.AthleticAmberTertiary
import com.kineticcare.core.designsystem.theme.KineticTealPrimary
import com.kineticcare.core.designsystem.theme.RedFlagError
import com.kineticcare.core.localization.toPersianDigits
import kotlin.math.roundToInt

/**
 * داشبورد تطبیق‌پذیر مراجع با ثبت نمره درد، رگرسیون هوشمند تمرین، و سیستم تبادل مواد غذایی
 * کلینیک ورزشی و حرکات اصلاحی دکتر قدم‌پور
 */
@Composable
fun AthleteDashboardScreen() {
    var painVasScore by remember { mutableFloatStateOf(2f) }
    var selectedPainRegion by remember { mutableStateOf("کمر و لگن") }
    val painRegions = listOf("کمر و لگن", "گردن و شانه", "زانو", "مچ پا")

    // متغیرهای بخش جایگزینی غذایی
    var selectedExchangeTarget by remember { mutableStateOf("ماهی قزل‌آلا") }

    val isPainHigh = painVasScore >= 5f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ۱. عنوان داشبورد
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "برنامه تمرینی تطبیقی مراجع",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "نظارت علمی: دکتر قدم‌پور",
                    style = MaterialTheme.typography.labelSmall,
                    color = KineticTealPrimary
                )
            }

            Surface(
                color = if (isPainHigh) RedFlagError.copy(alpha = 0.15f) else KineticTealPrimary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isPainHigh) "وضعیت: هشدار بالینی (Red Flag)" else "وضعیت: ایمن و آماده تمرین",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isPainHigh) RedFlagError else KineticTealPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ۲. ابزار ثبت درد پیش از تمرین (Pre-Workout VAS Pain Scale)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Healing,
                        contentDescription = null,
                        tint = if (isPainHigh) RedFlagError else KineticTealPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "مقیاس دیداری سنجش درد (VAS) قبل از تمرین",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "در صورت وجود درد بیش از نمره ۵، سیستم به طور خودکار حرکات سنگین را با تمرینات اصلاحی ایمن جایگزین می‌کند.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // انتخاب ناحیه درد
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    painRegions.forEach { region ->
                        val isSelected = (region == selectedPainRegion)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) KineticTealPrimary else MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedPainRegion = region }
                        ) {
                            Text(
                                text = region,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // اسلایدر سنجش نمره درد ۰ تا ۱۰
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "نمره درد: ${painVasScore.roundToInt().toPersianDigits()} از ۱۰",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isPainHigh) RedFlagError else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = when (painVasScore.roundToInt()) {
                            0 -> "بدون درد"
                            in 1..3 -> "درد خفیف"
                            in 4..6 -> "درد متوسط (آستانه رگرسیون)"
                            in 7..9 -> "درد شدید"
                            else -> "درد غیرقابل تحمل"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isPainHigh) RedFlagError else KineticTealPrimary
                    )
                }

                Slider(
                    value = painVasScore,
                    onValueChange = { painVasScore = it },
                    valueRange = 0f..10f,
                    steps = 9,
                    colors = SliderDefaults.colors(
                        thumbColor = if (isPainHigh) RedFlagError else KineticTealPrimary,
                        activeTrackColor = if (isPainHigh) RedFlagError else KineticTealPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ۳. کارت لیست حرکات تمرینی با رگرسیون هوشمند بر پایه درد
        Text(
            text = "حرکات جلسه امروز (جلسه تخصصی زنجیره خلفی):",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // حرکت اول: در صورت درد بالای ۵ رگرسیون می‌شود
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isPainHigh) Color(0xFFFEF2F2) else MaterialTheme.colorScheme.surface
            ),
            border = if (isPainHigh) androidx.compose.foundation.BorderStroke(1.dp, RedFlagError) else null
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isPainHigh) {
                            "حرکت ۱: پل باسن روی زمین (Glute Bridge) - رگرسیون شده"
                        } else {
                            "حرکت ۱: ددلیفت رومانیایی با هالتر (RDL)"
                        },
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isPainHigh) RedFlagError else MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        color = if (isPainHigh) RedFlagError else KineticTealPrimary,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (isPainHigh) "رگرسیون فعال" else "حرکت اصلی",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (isPainHigh) {
                    Text(
                        text = "علت تغییر: نمره درد شما در ناحیه $selectedPainRegion برابر ${painVasScore.roundToInt().toPersianDigits()} است. حرکت ددلیفت قفل شد و به پل باسن تبدیل گردید.",
                        style = MaterialTheme.typography.bodySmall,
                        color = RedFlagError
                    )
                } else {
                    Text(
                        text = "۳ ست ۱۰ تکرار | شاخص درک سختی (RPE): ۷ | تمرکز بر ثبات لگن",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ۴. سیستم جایگزینی واحدهای غذایی (Food Exchange Engine UI)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = AthleticAmberTertiary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "سیستم جایگزینی هوشمند واحدهای غذایی",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "وعده ناهار: ۱۵۰ گرم سینه مرغ پخته (واحد پروتئین خالص)",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "تعویض با منوی معادل بدون تغییر درشت‌مغذی‌ها:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                val options = listOf("ماهی قزل‌آلا", "سفیده تخم‌مرغ", "کنسرو ماهی تن")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    options.forEach { item ->
                        val isSelected = (item == selectedExchangeTarget)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) AthleticAmberTertiary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedExchangeTarget = item }
                        ) {
                            Text(
                                text = item,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // محاسبه خودکار وزن معادل
                val equivalentGrams = when (selectedExchangeTarget) {
                    "ماهی قزل‌آلا" -> "۱۸۰ گرم ماهی قزل‌آلا کبابی"
                    "سفیده تخم‌مرغ" -> "۴ عدد سفیده تخم‌مرغ آب‌پز (۱۴۰ گرم)"
                    "کنسرو ماهی تن" -> "۱۳۰ گرم فیله تن ماهی در آب‌نمک"
                    else -> "معادل استاندارد"
                }

                Surface(
                    color = AthleticAmberTertiary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = AthleticAmberTertiary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "مقدار مجاز جایگزین: $equivalentGrams",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = AthleticAmberTertiary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
