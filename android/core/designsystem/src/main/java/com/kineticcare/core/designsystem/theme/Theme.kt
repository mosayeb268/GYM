package com.kineticcare.core.designsystem.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat
import com.kineticcare.core.designsystem.typography.KineticTypography

private val DarkColorScheme = darkColorScheme(
    primary = KineticTealPrimary,
    onPrimary = KineticTealOnPrimary,
    primaryContainer = KineticTealContainer,
    onPrimaryContainer = KineticTealOnContainer,
    secondary = BiomechanicSlateSecondary,
    onSecondary = BiomechanicSlateOnSecondary,
    secondaryContainer = BiomechanicSlateContainer,
    onSecondaryContainer = BiomechanicSlateOnContainer,
    tertiary = AthleticAmberTertiary,
    onTertiary = AthleticAmberOnTertiary,
    tertiaryContainer = AthleticAmberContainer,
    onTertiaryContainer = AthleticAmberOnContainer,
    error = RedFlagError,
    onError = RedFlagOnError,
    errorContainer = RedFlagErrorContainer,
    onErrorContainer = RedFlagOnErrorContainer,
    background = SurfaceDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = KineticTealPrimary,
    onPrimary = KineticTealOnPrimary,
    primaryContainer = KineticTealContainer,
    onPrimaryContainer = KineticTealOnContainer,
    secondary = BiomechanicSlateSecondary,
    onSecondary = BiomechanicSlateOnSecondary,
    secondaryContainer = BiomechanicSlateContainer,
    onSecondaryContainer = BiomechanicSlateOnContainer,
    tertiary = AthleticAmberTertiary,
    onTertiary = AthleticAmberOnTertiary,
    tertiaryContainer = AthleticAmberContainer,
    onTertiaryContainer = AthleticAmberOnContainer,
    error = RedFlagError,
    onError = RedFlagOnError,
    errorContainer = RedFlagErrorContainer,
    onErrorContainer = RedFlagOnErrorContainer,
    background = SurfaceLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight
)

/**
 * تم جامع اپلیکیشن کینتیک کر با پشتیبانی اجباری از چیدمان راست‌به‌چپ (RTL) و قلم وزیرمتن
 * ویژه کلینیک ورزشی و حرکات اصلاحی دکتر قدم‌پور
 */
@Composable
fun KineticCareTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.surface.toArgb()
                window.navigationBarColor = colorScheme.surface.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    // الزام قطعی چیدمان راست‌به‌چپ (RTL) برای تمامی مؤلفه‌های سیستم
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = KineticTypography,
            content = content
        )
    }
}
