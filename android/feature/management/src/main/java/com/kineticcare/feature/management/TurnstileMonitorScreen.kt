package com.kineticcare.feature.management

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kineticcare.core.designsystem.theme.AthleticAmberTertiary
import com.kineticcare.core.designsystem.theme.KineticTealPrimary
import com.kineticcare.core.designsystem.theme.RedFlagError
import com.kineticcare.core.localization.toPersianDigits

data class AttendanceFeedItem(
    val id: String,
    val athleteName: String,
    val timeFa: String,
    val isSuccess: Boolean,
    val planTitle: String,
    val remainingSessionsFa: String,
    val assignedLockerNumber: Int?,
    val rejectReason: String? = null
)

data class AbsenteeClientItem(
    val fullName: String,
    val daysAbsent: Int,
    val phone: String
)

/**
 * صفحه مانیتورینگ زنده تردد گیت پذیرش، تخصیص کمد هوشمند و پایش غیبت ورزشکاران
 * کلینیک ورزشی و حرکات اصلاحی دکتر قدم‌پور
 */
@Composable
fun TurnstileMonitorScreen() {
    val attendanceLogs = remember {
        listOf(
            AttendanceFeedItem("1", "سارا احمدی", "۱۸:۴۵", true, "پکیج ترکیبی VIP", "۱۸ از ۲۴ جلسه", 12),
            AttendanceFeedItem("2", "علی رستمی", "۱۸:۳۲", true, "۱ ماهه نامحدود بدنسازی", "نامحدود", 15),
            AttendanceFeedItem("3", "محمد رضایی", "۱۸:۱۵", false, "۱۲ جلسه‌ای عمومی", "۰ از ۱۲ جلسه", null, "اتمام سهمیه جلسات"),
            AttendanceFeedItem("4", "نیلوفر کریمی", "۱۷:۵۸", true, "۱۲ جلسه‌ای اصلاحی VIP", "۸ از ۱۲ جلسه", 22)
        )
    }

    val absentClients = remember {
        listOf(
            AbsenteeClientItem("مهدی عباسی", 9, "09121112233"),
            AbsenteeClientItem("فاطمه کاظمی", 8, "09124445566")
        )
    }

    var sentSmsState by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // هدر کنترل گیت
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(Color(0xFF22C55E), CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "گیت تردد ورودی فعال است (GATE_01)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "کنترل تردد خودکار با بارکد پویای HMAC و کمد هوشمند",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { /* باز کردن دستی گیت */ },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.MeetingRoom, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "باز کردن اضطراری گیت", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // بخش هشدار غیبت ورزشکاران و پیامک CRM
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = AthleticAmberTertiary.copy(alpha = 0.12f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, AthleticAmberTertiary.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.NotificationImportant, contentDescription = null, tint = AthleticAmberTertiary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "پایش غیبت و پیشگیری از ریزش اعضا (CRM Alert):",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = AthleticAmberTertiary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    absentClients.forEach { c ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${c.fullName} (${c.daysAbsent.toPersianDigits()} روز غیبت متوالی)",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Button(
                                onClick = { sentSmsState = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AthleticAmberTertiary),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (sentSmsState) "پیامک ارسال شد" else "ارسال پیامک یادآوری",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }

        // فید زنده عبور و مرور ورزشکاران
        item {
            Text(
                text = "آخرین تردد‌های ثبت‌شده در گیت ورودی:",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        items(attendanceLogs) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(
                                    if (item.isSuccess) KineticTealPrimary.copy(alpha = 0.15f) else RedFlagError.copy(alpha = 0.15f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (item.isSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (item.isSuccess) KineticTealPrimary else RedFlagError,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = item.athleteName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (item.isSuccess) {
                                    "${item.planTitle} | ${item.remainingSessionsFa.toPersianDigits()}"
                                } else {
                                    "خطا: ${item.rejectReason}"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (item.isSuccess) MaterialTheme.colorScheme.onSurfaceVariant else RedFlagError
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = item.timeFa.toPersianDigits(),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (item.assignedLockerNumber != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "کمد: ${item.assignedLockerNumber.toPersianDigits()}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
