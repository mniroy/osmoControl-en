package com.alliot.osmo.demo.app.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkAppColors = darkColorScheme(
    primary = AccentBlue,
    onPrimary = ActionInk,
    secondary = AccentBlue,
    onSecondary = ActionInk,
    tertiary = SignalWarn,
    onTertiary = ActionInk,
    background = NightBackground,
    onBackground = NightForeground,
    surface = NightSurface,
    onSurface = NightForeground,
    surfaceVariant = NightSurfaceElevated,
    onSurfaceVariant = NightForegroundMuted,
    outline = NightOutline,
    outlineVariant = NightSurfaceElevated,
    error = SignalDanger,
    onError = ActionInk,
)

private val LightAppColors = lightColorScheme(
    primary = AccentBlueStrong,
    onPrimary = DaySurface,
    secondary = AccentBlueStrong,
    onSecondary = DaySurface,
    tertiary = SignalWarn,
    onTertiary = ActionInk,
    background = DayBackground,
    onBackground = DayForeground,
    surface = DaySurface,
    onSurface = DayForeground,
    surfaceVariant = DaySurfaceElevated,
    onSurfaceVariant = DayForegroundMuted,
    outline = DayOutline,
    outlineVariant = DaySurfaceElevated,
    error = SignalDanger,
    onError = ActionInk,
)

@Composable
fun OsmoDemoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkAppColors else LightAppColors
    ApplySystemBarStyle(
        darkTheme = darkTheme,
        background = colorScheme.background,
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}

@Composable
private fun ApplySystemBarStyle(
    darkTheme: Boolean,
    background: Color,
) {
    val view = LocalView.current
    if (view.isInEditMode) return

    SideEffect {
        val window = view.context.findActivity()?.window ?: return@SideEffect
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            android.graphics.Color.TRANSPARENT
        } else {
            background.toArgb()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
