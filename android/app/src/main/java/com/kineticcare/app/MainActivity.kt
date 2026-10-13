package com.kineticcare.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.SwapHoriz
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kineticcare.core.designsystem.components.PersianDatePickerDialog
import com.kineticcare.core.designsystem.theme.KineticCareTheme
import com.kineticcare.core.designsystem.theme.KineticTealPrimary
import com.kineticcare.core.localization.JalaliCalendarEngine
import com.kineticcare.core.localization.JalaliDate
import com.kineticcare.core.localization.mirrorForRtl
import com.kineticcare.core.localization.toPersianDigits
import com.kineticcare.core.localization.toToman
import com.kineticcare.feature.assessment.AssessmentReportScreen
import com.kineticcare.feature.assessment.GhostOverlayScreen
import com.kineticcare.feature.assessment.PosturalCaptureScreen
import com.kineticcare.feature.dashboard.AthleteDashboardScreen
import com.kineticcare.feature.management.ClientRegistrationScreen
import com.kineticcare.feature.management.SubscriptionAccountingScreen
import com.kineticcare.feature.management.TurnstileMonitorScreen
import com.kineticcare.feature.turnstile.TurnstilePassScreen

/**
 * اکتیویتی اصلی سیستم عامل کینتیک کر (Kinetic Care OS)
 * کلینیک ورزشی و حرکات اصلاحی دکتر قدم‌پور (Ph.D. فیزیولوژی ورزشی)
 * چیدمان ۱۰۰٪ راست‌به‌چپ (RTL) با امکان تغییر حالت میان مراجع ورزشکار و پنل پذیرش/مدیریت
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                KineticCareTheme {
                    KineticMainScaffold()
                }
            }
        }
    }
}

/**
 * حالت‌های عملیاتی سیستم: ورزشکار (Athlete) یا پذیرش/مدیریت کلینیک (Management)
 */
enum class UserRoleMode(val titleFa: String, val badgeTextFa: String) {
    ATHLETE("ورزشکار", "پنل ورزشکار"),
    MANAGEMENT("پذیرش و مدیریت", "پنل مدیریت")
}

sealed class ScreenTab(val titleFa: String, val icon: ImageVector) {
    // تب‌های ورزشکار
    object Home : ScreenTab("خانه", Icons.Default.Home)
    object Dashboard : ScreenTab("تمرین و درد", Icons.Default.FitnessCenter)
    object CameraAssessment : ScreenTab("شاقول دوربین", Icons.Default.CameraAlt)
    object GhostCompare : ScreenTab("مقایسه قامت", Icons.Default.CompareArrows)
    object AssessmentReport : ScreenTab("کارنامه بالینی", Icons.Default.Assignment)
    object TurnstilePass : ScreenTab("تردد گیت", Icons.Default.QrCode2)

    // تب‌های مدیریت و پذیرش
    object ClientRegistration : ScreenTab("پذیرش مراجع", Icons.Default.PersonAdd)
    object Accounting : ScreenTab("حسابداری و شهریه", Icons.Default.ReceiptLong)
    object TurnstileMonitor : ScreenTab("مانیتورینگ گیت", Icons.Default.MeetingRoom)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KineticMainScaffold() {
    var userMode by remember { mutableStateOf(UserRoleMode.ATHLETE) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val athleteTabs = listOf(
        ScreenTab.Home,
        ScreenTab.Dashboard,
        ScreenTab.CameraAssessment,
        ScreenTab.GhostCompare,
        ScreenTab.AssessmentReport,
        ScreenTab.TurnstilePass
    )

    val managementTabs = listOf(
        ScreenTab.ClientRegistration,
        ScreenTab.Accounting,
        ScreenTab.TurnstileMonitor,
        ScreenTab.CameraAssessment,
        ScreenTab.AssessmentReport
    )

    val currentTabs = if (userMode == UserRoleMode.ATHLETE) athleteTabs else managementTabs

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "کلینیک ورزشی و حرکات اصلاحی",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "دکتر قدم‌پور (Ph.D. فیزیولوژی) • ${userMode.badgeTextFa}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    // دکمه تغییر نقش میان ورزشکار و پذیرش
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable {
                                userMode = if (userMode == UserRoleMode.ATHLETE) {
                                    UserRoleMode.MANAGEMENT
                                } else {
                                    UserRoleMode.ATHLETE
                                }
                                selectedTab = 0
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "تغییر نقش",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (userMode == UserRoleMode.ATHLETE) "پنل پذیرش" else "حالت ورزشکار",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                currentTabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.titleFa,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.titleFa,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (userMode == UserRoleMode.ATHLETE) {
                when (selectedTab) {
                    0 -> KineticOverviewTab(
                        onNavigateToCamera = { selectedTab = 2 },
                        onNavigateToTurnstile = { selectedTab = 5 },
                        onNavigateToReport = { selectedTab = 4 },
                        onSwitchToManagement = {
                            userMode = UserRoleMode.MANAGEMENT
                            selectedTab = 0
                        }
                    )
                    1 -> AthleteDashboardScreen()
                    2 -> PosturalCaptureScreen()
                    3 -> GhostOverlayScreen()
                    4 -> AssessmentReportScreen(onNavigateBack = { selectedTab = 0 })
                    5 -> TurnstilePassScreen()
                }
            } else {
                when (selectedTab) {
                    0 -> ClientRegistrationScreen()
                    1 -> SubscriptionAccountingScreen()
                    2 -> TurnstileMonitorScreen()
                    3 -> PosturalCaptureScreen()
                    4 -> AssessmentReportScreen(onNavigateBack = { selectedTab = 0 })
                }
            }
        }
    }
}

@Composable
fun KineticOverviewTab(
    onNavigateToCamera: () -> Unit,
    onNavigateToTurnstile: () -> Unit,
    onNavigateToReport: () -> Unit,
    onSwitchToManagement: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(JalaliCalendarEngine.now()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // ۱. کارت خوش‌آمدگویی و مشخصات تاریخ شمسی امروز
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "امروز: ${JalaliCalendarEngine.now().toFullDescription()}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "شعبه مرکزی",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "سیستم یکپارچه مدیریت بدنسازی تخصصی و توانبخشی ساختار قامتی مراجعین",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ۲. وضعیت اشتراک و محاسبات مالی به تومان
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "اشتراک فعال: پکیج ترکیبی VIP",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "اعتبار جلسات باقیمانده",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "۱۸ از ۲۴ جلسه".toPersianDigits(),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "شهریه پرداختی (تومان)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = 3850000L.toToman(),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ۳. کارت هشدار بالینی و نظارت فیزیولوژی ورزشی (Red Flag Alert)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "هشدار بالینی",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "نظارت بالینی دکتری فیزیولوژی (Red Flag Gate)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "عارضه مراجع: هایپرلوردوز کمری شدید. حرکات متداخل ددلیفت به صورت خودکار رگرسیون شده و با پل باسن جایگزین گردیدند.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ۴. تقویم شمسی جلالی تعاملی
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "تاریخ نوبت ارزیابی پاسچرال:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = selectedDate.toFullDescription(),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { showDatePicker = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تغییر تاریخ نوبت ارزیابی (تقویم شمسی)",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ۵. دکمه‌های اقدام سریع (دوربین شاقول، کارنامه بالینی و بارکد تردد)
        Button(
            onClick = onNavigateToCamera,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "اجرای ارزیابی پاسچرال با دوربین و شاقول تراز",
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "ادامه",
                modifier = Modifier.mirrorForRtl()
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onNavigateToReport,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Assignment,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "مشاهده کارنامه ارزیابی و گزارش رسمی ارتوپدی",
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onNavigateToTurnstile,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.QrCode2,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "بارکد پویای تردد گیت ورودی",
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showDatePicker) {
        PersianDatePickerDialog(
            initialDate = selectedDate,
            title = "انتخاب تاریخ نوبت ارزیابی",
            onDismissRequest = { showDatePicker = false },
            onDateSelected = { newDate ->
                selectedDate = newDate
                showDatePicker = false
            }
        )
    }
}
