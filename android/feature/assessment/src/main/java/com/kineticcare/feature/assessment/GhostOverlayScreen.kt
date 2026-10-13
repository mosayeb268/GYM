package com.kineticcare.feature.assessment

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TrendingDown
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kineticcare.core.designsystem.theme.KineticTealPrimary
import com.kineticcare.core.localization.toPersianDigits
import kotlin.math.roundToInt

/**
 * ابزار مقایسه قبل و بعد اصلاح ساختار قامت (Ghost Overlay View)
 * مقایسه عکس جلسه اول ارزیابی با پایان ماه سوم حرکات اصلاحی
 * کلینیک ورزشی و حرکات اصلاحی دکتر قدم‌پور
 */
@Composable
fun GhostOverlayScreen(
    clientName: String = "سارا احمدی",
    onExportPdfReport: () -> Unit = {}
) {
    var overlayOpacity by remember { mutableFloatStateOf(0.5f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // هدر صفحه
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ابزار مقایسه تطبیقی قامت (Ghost Overlay)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "مراجع: $clientName | نمای لترال راست",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Button(
                onClick = onExportPdfReport,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "گزارش ارجاع پزشکی",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // باکس پیش‌نمایش تصویر روی‌هم‌افتاده با اسلایدر شفافیت (Ghost Overlay Canvas)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.85f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // لایه ۱: تصویر اولیه (جلسه اول - قبل اصلاح)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1E293B)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "تصویر پایه: جلسه اول (اردیبهشت ۱۴۰۳)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray
                        )
                        Text(
                            text = "زاویه لوردوز کمری: ۴۸° (هایپرلوردوز شدید)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFEF4444)
                        )
                    }
                }

                // لایه ۲: تصویر ثانویه متحرک (Ghost Overlay با آلفای اسلایدر)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(overlayOpacity)
                        .background(Color(0xFF0F766E).copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "تصویر پیشرفت: پایان ماه سوم (مرداد ۱۴۰۳)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                        Text(
                            text = "زاویه لوردوز کمری: ۳۴° (دامنه نرمال بیومکانیک)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF22C55E)
                        )
                    }
                }

                // بج راهنما
                Surface(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "شفافیت تطبیق: ${(overlayOpacity * 100).roundToInt().toPersianDigits()}٪",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // کنترلر اسلایدر شفافیت (Opacity Slider)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "تنظیم نسبت همپوشانی تصاویر:",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "جلسه اول (۰٪) ⟵ ⟶ ماه سوم (۱۰۰٪)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Slider(
                    value = overlayOpacity,
                    onValueChange = { overlayOpacity = it },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // کارت شاخص‌های اندازه‌گیری زاویه‌ای و بهبودی بیومکانیک مراجع
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "تحلیل فیزیولوژیک تغییرات زاویه‌ای ساختار قامتی:",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricComparisonItem(
                        titleFa = "تیلت قدامی لگن",
                        beforeFa = "۱۹° (شدید)",
                        afterFa = "۱۰° (نرمال)",
                        improvementPercentFa = "۴۷٪ بهبودی"
                    )

                    MetricComparisonItem(
                        titleFa = "عارضه سر به جلو",
                        beforeFa = "۴۴°",
                        afterFa = "۵۲°",
                        improvementPercentFa = "۱۸٪ اصلاح"
                    )
                }
            }
        }
    }
}

@Composable
fun MetricComparisonItem(
    titleFa: String,
    beforeFa: String,
    afterFa: String,
    improvementPercentFa: String
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .padding(12.dp)
            .width(150.dp)
    ) {
        Text(
            text = titleFa,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "قبل: ${beforeFa.toPersianDigits()}",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFEF4444)
        )
        Text(
            text = "بعد: ${afterFa.toPersianDigits()}",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF22C55E)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            color = Color(0xFFDCFCE7),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                text = improvementPercentFa.toPersianDigits(),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF166534),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}
