package me.elhizazi.Castix.ui.theme

import android.content.res.Configuration
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import me.elhizazi.Castix.privilege.ui.AppSettingsManager
import me.elhizazi.Castix.privilege.ui.AppThemeMode
import java.util.Locale

fun parseHexColor(hex: String, fallback: Color = SiwaneTeal): Color {
    return try {
        val cleanHex = hex.removePrefix("#")
        val colorInt = when (cleanHex.length) {
            6 -> 0xFF000000.toInt() or cleanHex.toInt(16)
            8 -> cleanHex.toLong(16).toInt()
            else -> return fallback
        }
        Color(colorInt)
    } catch (_: Exception) {
        fallback
    }
}

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val settingsManager = AppSettingsManager.getInstance(context)
    val appSettings by settingsManager.settings.collectAsState()

    val isDark = when (appSettings.themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val customAccent = parseHexColor(appSettings.accentColorHex, SiwaneTeal)

    val baseDark = darkColorScheme(
        primary = customAccent,
        onPrimary = Color.Black,
        primaryContainer = customAccent.copy(alpha = 0.22f),
        onPrimaryContainer = customAccent,
        secondary = SiwaneTealLight,
        onSecondary = Color.Black,
        secondaryContainer = Color(0xFF1E293B),
        onSecondaryContainer = Color(0xFFE2E8F0),
        tertiary = NeonGreen,
        onTertiary = Color.Black,
        background = DarkBg,
        onBackground = Color(0xFFF1F5F9),
        surface = DarkSurface,
        onSurface = Color(0xFFF8FAFC),
        surfaceVariant = DarkSurfaceElevated,
        onSurfaceVariant = Color(0xFF94A3B8),
        outline = DarkBorder,
        outlineVariant = Color(0xFF27354A)
    )

    val baseLight = lightColorScheme(
        primary = customAccent,
        onPrimary = Color.White,
        primaryContainer = customAccent.copy(alpha = 0.16f),
        onPrimaryContainer = customAccent,
        secondary = SiwaneTealLight,
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFE2E8F0),
        onSecondaryContainer = Color(0xFF1E293B),
        tertiary = NeonGreen,
        onTertiary = Color.White,
        background = LightBg,
        onBackground = Color(0xFF0F172A),
        surface = LightSurface,
        onSurface = Color(0xFF0F172A),
        surfaceVariant = LightSurfaceElevated,
        onSurfaceVariant = Color(0xFF64748B),
        outline = LightBorder,
        outlineVariant = Color(0xFFE2E8F0)
    )

    val colorScheme = when {
        appSettings.dynamicSystemColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> baseDark
        else -> baseLight
    }

    val effectiveLanguage = remember(appSettings.language) {
        AppSettingsManager.getEffectiveLanguage(appSettings.language)
    }
    val currentLocale = remember(effectiveLanguage) { Locale(effectiveLanguage) }
    val currentConfig = LocalConfiguration.current
    val localizedConfig = remember(currentConfig, currentLocale) {
        Configuration(currentConfig).apply {
            setLocale(currentLocale)
            setLayoutDirection(currentLocale)
        }
    }
    val localizedContext = remember(context, currentLocale) {
        context.createConfigurationContext(localizedConfig)
    }

    val layoutDirection = if (effectiveLanguage == "ar") {
        LayoutDirection.Rtl
    } else {
        LayoutDirection.Ltr
    }

    // Dynamic UI Scale
    val currentDensity = LocalDensity.current
    val scaleFactor = (appSettings.uiScalePercent / 100f).coerceIn(0.75f, 1.25f)
    val scaledDensity = remember(currentDensity, scaleFactor) {
        Density(
            density = currentDensity.density * scaleFactor,
            fontScale = currentDensity.fontScale * scaleFactor
        )
    }

    CompositionLocalProvider(
        LocalConfiguration provides localizedConfig,
        LocalContext provides localizedContext,
        LocalLayoutDirection provides layoutDirection,
        LocalDensity provides scaledDensity
    ) {
        MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
    }
}
