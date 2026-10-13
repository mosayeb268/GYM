package com.kineticcare.core.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

/**
 * ابزارها و مودیفایرهای مدیریت چیدمان راست‌به‌چپ (RTL) و آینه‌سازی آیکون‌ها
 * کلینیک ورزشی و حرکات اصلاحی دکتر قدم‌پور
 */
object RtlSupport {

    /**
     * ارائه‌دهنده جهت چیدمان راست‌به‌چپ (RTL) در ریشه برنامه یا بخش‌های خاص
     */
    @Composable
    fun ProvideRtl(content: @Composable () -> Unit) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            content()
        }
    }
}

/**
 * مودیفایر اختصاصی جهت آینه‌سازی خودکار آیکون‌های جهتی (Back Arrow, Forward, Step Next)
 * این تابع تنها زمانی که چیدمان فعلی RTL باشد، تصویر را به صورت افقی معکوس می‌کند.
 */
@Composable
fun Modifier.mirrorForRtl(): Modifier {
    val layoutDirection = LocalLayoutDirection.current
    return if (layoutDirection == LayoutDirection.Rtl) {
        this.scale(scaleX = -1f, scaleY = 1f)
    } else {
        this
    }
}

/**
 * مودیفایر آینه‌سازی بر اساس وضعیت مشخص جهت
 */
fun Modifier.mirrorForRtl(layoutDirection: LayoutDirection): Modifier {
    return if (layoutDirection == LayoutDirection.Rtl) {
        this.scale(scaleX = -1f, scaleY = 1f)
    } else {
        this
    }
}
