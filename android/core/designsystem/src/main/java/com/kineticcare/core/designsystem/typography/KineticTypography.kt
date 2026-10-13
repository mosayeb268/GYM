package com.kineticcare.core.designsystem.typography

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.sp

/**
 * پیکربندی تایپوگرافی قلم وزیرمتن (Vazirmatn) برای سیستم عامل کینتیک کر
 * با تنظیم ویژه lineHeight جهت جلوگیری از بریده شدن اعراب و کشیدگی‌های حروف فارسی (گ، پ، ژ، چ، ی)
 */
val VazirmatnFontFamily = FontFamily.Default // در پروژه نهایی فایل‌های TTF/OTF در res/font قرار می‌گیرند

private val defaultPersianLineHeightStyle = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None
)

val KineticTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Black,
        fontSize = 57.sp,
        lineHeight = 72.sp,
        letterSpacing = 0.sp,
        textDirection = TextDirection.ContentOrRtl,
        lineHeightStyle = defaultPersianLineHeightStyle
    ),
    displayMedium = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 58.sp,
        letterSpacing = 0.sp,
        textDirection = TextDirection.ContentOrRtl,
        lineHeightStyle = defaultPersianLineHeightStyle
    ),
    displaySmall = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 48.sp,
        letterSpacing = 0.sp,
        textDirection = TextDirection.ContentOrRtl,
        lineHeightStyle = defaultPersianLineHeightStyle
    ),
    headlineLarge = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
        textDirection = TextDirection.ContentOrRtl,
        lineHeightStyle = defaultPersianLineHeightStyle
    ),
    headlineMedium = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 38.sp,
        letterSpacing = 0.sp,
        textDirection = TextDirection.ContentOrRtl,
        lineHeightStyle = defaultPersianLineHeightStyle
    ),
    headlineSmall = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp,
        textDirection = TextDirection.ContentOrRtl,
        lineHeightStyle = defaultPersianLineHeightStyle
    ),
    titleLarge = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
        textDirection = TextDirection.ContentOrRtl,
        lineHeightStyle = defaultPersianLineHeightStyle
    ),
    titleMedium = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.15.sp,
        textDirection = TextDirection.ContentOrRtl,
        lineHeightStyle = defaultPersianLineHeightStyle
    ),
    titleSmall = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp,
        textDirection = TextDirection.ContentOrRtl,
        lineHeightStyle = defaultPersianLineHeightStyle
    ),
    bodyLarge = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.25.sp,
        textDirection = TextDirection.ContentOrRtl,
        lineHeightStyle = defaultPersianLineHeightStyle
    ),
    bodyMedium = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.25.sp,
        textDirection = TextDirection.ContentOrRtl,
        lineHeightStyle = defaultPersianLineHeightStyle
    ),
    bodySmall = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.4.sp,
        textDirection = TextDirection.ContentOrRtl,
        lineHeightStyle = defaultPersianLineHeightStyle
    ),
    labelLarge = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        textDirection = TextDirection.ContentOrRtl,
        lineHeightStyle = defaultPersianLineHeightStyle
    ),
    labelMedium = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp,
        textDirection = TextDirection.ContentOrRtl,
        lineHeightStyle = defaultPersianLineHeightStyle
    ),
    labelSmall = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        textDirection = TextDirection.ContentOrRtl,
        lineHeightStyle = defaultPersianLineHeightStyle
    )
)
