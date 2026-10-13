package com.kineticcare.feature.turnstile

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kineticcare.core.designsystem.theme.KineticTealPrimary
import com.kineticcare.core.localization.toPersianDigits
import kotlinx.coroutines.delay
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * صفحه بلیط هوشمند تردد با بارکد پویای رمزنگاری شده (Dynamic QR Turnstile Pass)
 * بازتولید خودکار هر ۱۵ ثانیه با تایمر معکوس فارسی و تم دارک ورزشی
 * کلینیک ورزشی و حرکات اصلاحی دکتر قدم‌پور
 */
@Composable
fun TurnstilePassScreen(
    userId: String = "client-user-9823",
    athleteName: String = "سارا احمدی",
    planTitle: String = "پکیج ترکیبی VIP: اصلاحی بالینی + بدنسازی",
    remainingSessions: Int = 18,
    totalSessions: Int = 24,
    expiryDateJalali: String = "۱۴۰۳/۰۹/۲۸"
) {
    var countdownSeconds by remember { mutableIntStateOf(15) }
    var currentToken by remember { mutableStateOf("") }

    // بازتولید توکن رمزنگاری شده HMAC-SHA256 هر ۱۵ ثانیه
    LaunchedEffect(countdownSeconds) {
        if (countdownSeconds <= 0) {
            countdownSeconds = 15
        }
        delay(1000L)
        countdownSeconds--
    }

    LaunchedEffect(countdownSeconds == 15) {
        val timestamp = System.currentTimeMillis() / 1000
        val payload = "$userId.$timestamp"
        val hmac = Mac.getInstance("HmacSHA256").apply {
            init(SecretKeySpec("kinetic_care_qr_hmac_secret_key_2026".toByteArray(), "HmacSHA256"))
        }
        val signature = hmac.doFinal(payload.toByteArray()).joinToString("") { "%02x".format(it) }.take(32)
        currentToken = "$userId.$timestamp.$signature"
    }

    val progress = countdownSeconds / 15f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "countdown"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // دارک تم شیک ورزشی
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // مشخصات کلینیک دکتر قدم‌پور
        Text(
            text = "کلینیک ورزشی و حرکات اصلاحی دکتر قدم‌پور",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
        Text(
            text = "بلیط تردد هوشمند گیت (Dynamic Turnstile Pass)",
            style = MaterialTheme.typography.bodySmall,
            color = KineticTealPrimary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // کارت اصلی کیوآرکد پویا
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // کارت مشخصات مراجع
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(KineticTealPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = athleteName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = planTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // پیش‌نمایش گرافیکی ماتریکس بارکد پویا (QR Code Render Box)
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .size(220.dp)
                        .padding(4.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        QrCodeCanvasMatrix(seed = currentToken)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // تایمر معکوس ۱۵ ثانیه‌ای با ارقام فارسی
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.size(44.dp),
                            color = KineticTealPrimary,
                            trackColor = Color(0xFF334155),
                            strokeWidth = 4.dp
                        )
                        Text(
                            text = countdownSeconds.toPersianDigits(),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "اعتبار بارکد: ${countdownSeconds.toPersianDigits()} ثانیه",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "تولید مجدد خودکار جهت جلوگیری از تقلب و سوءاستفاده",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.LightGray
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // وضعیت اعتبار اشتراک و سهمیه جلسات
        Card(
            modifier = Modifier.fillMaxWidth(0.92f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "مانده سهمیه جلسات:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.LightGray
                    )
                    Text(
                        text = "$remainingSessions از $totalSessions جلسه".toPersianDigits(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = KineticTealPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "تاریخ پایان اعتبار:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.LightGray
                    )
                    Text(
                        text = expiryDateJalali.toPersianDigits(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // راهنمای استفاده جلوی گیت
        Surface(
            color = Color(0xFF1E293B).copy(alpha = 0.6f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LockClock,
                    contentDescription = null,
                    tint = KineticTealPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "صفحه نمایش را در فاصله ۱۵ سانتیمتری اسکنر گیت ورودی کلینیک قرار دهید.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * رندر ماتریس شبیه‌ساز بارکد QR متناسب با توکن هش‌شده
 */
@Composable
fun QrCodeCanvasMatrix(seed: String) {
    Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        val gridSize = 19
        val cellSize = size.width / gridSize
        val hash = seed.hashCode()

        for (x in 0 until gridSize) {
            for (y in 0 until gridSize) {
                // رسم مارکرهای موقعیت در گوشه‌ها (Finder Patterns)
                val isCornerMarker = (x in 0..4 && y in 0..4) ||
                        (x in 0..4 && y in (gridSize - 5) until gridSize) ||
                        (x in (gridSize - 5) until gridSize && y in 0..4)

                val shouldFill = if (isCornerMarker) {
                    (x == 0 || x == 4 || y == 0 || y == 4 || (x in 1..3 && y in 1..3))
                } else {
                    ((x * 7 + y * 13 + hash) % 3) == 0
                }

                if (shouldFill) {
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(x * cellSize, y * cellSize),
                        size = Size(cellSize, cellSize)
                    )
                }
            }
        }
    }
}
