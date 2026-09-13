package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.viewmodel.AppThemeMode

@Composable
fun MyApplicationTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    accentIndex: Int = 0,
    content: @Composable () -> Unit
) {
    val primaryAccent = when (accentIndex) {
        1 -> NeonViolet
        2 -> EmeraldGreen
        3 -> SunsetAmber
        else -> ElectricCyan
    }

    val primaryContainer = when (accentIndex) {
        1 -> NeonVioletContainer
        2 -> EmeraldGreenContainer
        3 -> SunsetAmberContainer
        else -> ElectricCyanContainer
    }

    val colorScheme = when (themeMode) {
        AppThemeMode.AMOLED -> darkColorScheme(
            primary = primaryAccent,
            primaryContainer = primaryContainer,
            background = AmoledBg,
            surface = AmoledSurface,
            surfaceVariant = AmoledSurfaceVariant,
            onBackground = DarkTextPrimary,
            onSurface = DarkTextPrimary,
            onSurfaceVariant = DarkTextSecondary,
            outline = AmoledCardBorder
        )
        AppThemeMode.LIGHT -> lightColorScheme(
            primary = if (accentIndex == 0) ElectricCyanDark else primaryAccent,
            primaryContainer = primaryContainer,
            background = LightBg,
            surface = LightSurface,
            surfaceVariant = LightSurfaceVariant,
            onBackground = LightTextPrimary,
            onSurface = LightTextPrimary,
            onSurfaceVariant = LightTextSecondary,
            outline = LightCardBorder
        )
        AppThemeMode.DARK -> darkColorScheme(
            primary = primaryAccent,
            primaryContainer = primaryContainer,
            background = DarkBg,
            surface = DarkSurface,
            surfaceVariant = DarkSurfaceVariant,
            onBackground = DarkTextPrimary,
            onSurface = DarkTextPrimary,
            onSurfaceVariant = DarkTextSecondary,
            outline = DarkCardBorder
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
