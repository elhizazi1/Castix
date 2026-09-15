package me.elhizazi.Castix.privilege.ui

import android.content.Context
import android.content.SharedPreferences
import me.elhizazi.Castix.privilege.domain.model.EngineType
import me.elhizazi.Castix.privilege.domain.model.PlaybackOptimizationTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class ClockStyle {
    DIGITAL_CLEAN,   // Modern HH:mm with subtle date
    MINIMAL_COMPACT, // Small time, battery & background app badge
    LARGE_GLOW,      // Futuristic large glow digits
    ANALOG_MINIMAL,  // Minimal circular dial with clock hands
    RETRO_FLIP,      // Retro digital flip card style
    TEXT_WORD,       // Elegant textual clock
    PIXEL_AIRY,      // Pixel-inspired airy typography
    MATERIAL_EXPRESSIVE, // Expressive Material-style clock
    LARGE_CENTER,     // Oversized centered digits
    STACKED,          // Hours and minutes stacked vertically
    MONO_LINE,        // Monospaced compact line
    DATA_DASHBOARD   // Clock with a compact data dashboard
}

data class AppAppSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val language: String = "auto", // follows the device language; unsupported languages fall back to English
    val accentColorHex: String = "#14B8A6", // Castix Teal default
    val dynamicSystemColor: Boolean = false,
    val clockStyle: ClockStyle = ClockStyle.DIGITAL_CLEAN,
    val clockScalePercent: Int = 100,
    val clockVerticalOffsetDp: Int = 0,
    val clockSpacingPercent: Int = 100,
    val showAppBadgeOnBlackScreen: Boolean = true,
    val showBatteryOnBlackScreen: Boolean = true,
    val showDateOnBlackScreen: Boolean = true,
    val burnInProtection: Boolean = true,
    val showPlaybackTimer: Boolean = true,
    val customInscription: String = "",
    val floatingButtonSizeDp: Int = 54, // 36dp to 76dp
    val floatingButtonAlphaPercent: Int = 90, // 20% to 100%
    val floatingHapticFeedback: Boolean = true,
    val uiScalePercent: Int = 100 // 80% to 120%
)

class AppSettingsManager private constructor(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("castix_global_app_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppAppSettings> = _settings.asStateFlow()

    companion object {
        @Volatile
        private var instance: AppSettingsManager? = null

        fun getInstance(context: Context): AppSettingsManager {
            return instance ?: synchronized(this) {
                instance ?: AppSettingsManager(context.applicationContext).also { instance = it }
            }
        }

        fun getEffectiveLanguage(lang: String): String {
            return AppStrings.resolveEffectiveLanguage(lang)
        }
    }

    private fun loadSettings(): AppAppSettings {
        val themeStr = prefs.getString("theme_mode", AppThemeMode.SYSTEM.name) ?: AppThemeMode.DARK.name
        val themeMode = try { AppThemeMode.valueOf(themeStr) } catch (_: Exception) { AppThemeMode.SYSTEM }
        val lang = prefs.getString("language", "auto") ?: "auto"
        val hex = prefs.getString("accent_hex", "#00E5FF") ?: "#00E5FF"
        val dynamic = prefs.getBoolean("dynamic_color", true)
        val clockStr = prefs.getString("clock_style", ClockStyle.DIGITAL_CLEAN.name) ?: ClockStyle.DIGITAL_CLEAN.name
        val clockStyle = try { ClockStyle.valueOf(clockStr) } catch (_: Exception) { ClockStyle.DIGITAL_CLEAN }
        val clockScale = prefs.getInt("clock_scale_percent", 100)
        val clockOffset = prefs.getInt("clock_vertical_offset_dp", 0)
        val clockSpacing = prefs.getInt("clock_spacing_percent", 100)
        val showBadge = prefs.getBoolean("show_badge_black_screen", true)
        val showBattery = prefs.getBoolean("show_battery_black_screen", true)
        val showDate = prefs.getBoolean("show_date_black_screen", true)
        val burnIn = prefs.getBoolean("burn_in_protection", true)
        val showTimer = prefs.getBoolean("show_playback_timer", true)
        val inscription = prefs.getString("custom_inscription", "") ?: ""
        val buttonSize = prefs.getInt("floating_btn_size", 54)
        val buttonAlpha = prefs.getInt("floating_btn_alpha", 90)
        val floatingHaptic = prefs.getBoolean("floating_haptic_feedback", true)
        val uiScale = prefs.getInt("ui_scale_percent", 100)

        return AppAppSettings(
            themeMode = themeMode,
            language = lang,
            accentColorHex = hex,
            dynamicSystemColor = dynamic,
            clockStyle = clockStyle,
            clockScalePercent = clockScale.coerceIn(75, 140),
            clockVerticalOffsetDp = clockOffset.coerceIn(-80, 80),
            clockSpacingPercent = clockSpacing.coerceIn(60, 150),
            showAppBadgeOnBlackScreen = showBadge,
            showBatteryOnBlackScreen = showBattery,
            showDateOnBlackScreen = showDate,
            burnInProtection = burnIn,
            showPlaybackTimer = showTimer,
            customInscription = inscription,
            floatingButtonSizeDp = buttonSize,
            floatingButtonAlphaPercent = buttonAlpha,
            floatingHapticFeedback = floatingHaptic,
            uiScalePercent = uiScale
        )
    }

    init {
        applyLocale(_settings.value.language)
    }

    private fun applyLocale(lang: String) {
        try {
            val effectiveLang = AppStrings.resolveEffectiveLanguage(lang)
            val locale = java.util.Locale(effectiveLang)
            java.util.Locale.setDefault(locale)
            val res = context.resources
            val config = res.configuration
            config.setLocale(locale)
            config.setLayoutDirection(locale)
            @Suppress("DEPRECATION")
            res.updateConfiguration(config, res.displayMetrics)
        } catch (_: Exception) {}
    }

    fun updateThemeMode(mode: AppThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _settings.update { it.copy(themeMode = mode) }
    }

    fun updateLanguage(lang: String) {
        prefs.edit().putString("language", lang).apply()
        applyLocale(lang)
        _settings.update { it.copy(language = lang) }
    }

    fun updateAccentColor(hex: String, dynamicColor: Boolean = false) {
        prefs.edit().putString("accent_hex", hex).putBoolean("dynamic_color", dynamicColor).apply()
        _settings.update { it.copy(accentColorHex = hex, dynamicSystemColor = dynamicColor) }
    }

    fun updateClockStyle(style: ClockStyle) {
        prefs.edit().putString("clock_style", style.name).apply()
        _settings.update { it.copy(clockStyle = style) }
    }

    fun updateClockScale(percent: Int) {
        val value = percent.coerceIn(75, 140)
        prefs.edit().putInt("clock_scale_percent", value).apply()
        _settings.update { it.copy(clockScalePercent = value) }
    }

    fun updateClockVerticalOffset(dp: Int) {
        val value = dp.coerceIn(-80, 80)
        prefs.edit().putInt("clock_vertical_offset_dp", value).apply()
        _settings.update { it.copy(clockVerticalOffsetDp = value) }
    }

    fun updateClockSpacing(percent: Int) {
        val value = percent.coerceIn(60, 150)
        prefs.edit().putInt("clock_spacing_percent", value).apply()
        _settings.update { it.copy(clockSpacingPercent = value) }
    }

    fun updateShowAppBadge(show: Boolean) {
        prefs.edit().putBoolean("show_badge_black_screen", show).apply()
        _settings.update { it.copy(showAppBadgeOnBlackScreen = show) }
    }

    fun updateShowBattery(show: Boolean) {
        prefs.edit().putBoolean("show_battery_black_screen", show).apply()
        _settings.update { it.copy(showBatteryOnBlackScreen = show) }
    }

    fun updateShowDate(show: Boolean) {
        prefs.edit().putBoolean("show_date_black_screen", show).apply()
        _settings.update { it.copy(showDateOnBlackScreen = show) }
    }

    fun updateBurnInProtection(enabled: Boolean) {
        prefs.edit().putBoolean("burn_in_protection", enabled).apply()
        _settings.update { it.copy(burnInProtection = enabled) }
    }

    fun updateShowPlaybackTimer(show: Boolean) {
        prefs.edit().putBoolean("show_playback_timer", show).apply()
        _settings.update { it.copy(showPlaybackTimer = show) }
    }

    fun updateCustomInscription(text: String) {
        prefs.edit().putString("custom_inscription", text).apply()
        _settings.update { it.copy(customInscription = text) }
    }

    fun updateFloatingButtonSize(sizeDp: Int) {
        val clamped = sizeDp.coerceIn(36, 76)
        prefs.edit().putInt("floating_btn_size", clamped).apply()
        _settings.update { it.copy(floatingButtonSizeDp = clamped) }
    }

    fun updateFloatingButtonAlpha(alphaPercent: Int) {
        val clamped = alphaPercent.coerceIn(20, 100)
        prefs.edit().putInt("floating_btn_alpha", clamped).apply()
        _settings.update { it.copy(floatingButtonAlphaPercent = clamped) }
    }

    fun updateFloatingHapticFeedback(enabled: Boolean) {
        prefs.edit().putBoolean("floating_haptic_feedback", enabled).apply()
        _settings.update { it.copy(floatingHapticFeedback = enabled) }
    }

    fun updateUiScale(scalePercent: Int) {
        val clamped = scalePercent.coerceIn(80, 120)
        prefs.edit().putInt("ui_scale_percent", clamped).apply()
        _settings.update { it.copy(uiScalePercent = clamped) }
    }

    // --- Privilege Engine & Playback Task Persistence ---

    fun getSelectedEngine(): EngineType? {
        val name = prefs.getString("selected_privilege_engine", null) ?: return null
        return runCatching { EngineType.valueOf(name) }.getOrNull()
    }

    fun saveSelectedEngine(engine: EngineType) {
        prefs.edit().putString("selected_privilege_engine", engine.name).apply()
    }

    fun getEnforcedPlaybackTasks(): Set<PlaybackOptimizationTask> {
        val rawSet = prefs.getStringSet("enforced_playback_tasks", emptySet()) ?: emptySet()
        return rawSet.mapNotNull { name ->
            runCatching { PlaybackOptimizationTask.valueOf(name) }.getOrNull()
        }.toSet()
    }

    fun saveEnforcedPlaybackTasks(tasks: Set<PlaybackOptimizationTask>) {
        val names = tasks.map { it.name }.toSet()
        prefs.edit().putStringSet("enforced_playback_tasks", names).apply()
    }

    fun setTaskEnforced(task: PlaybackOptimizationTask, enforced: Boolean) {
        val current = getEnforcedPlaybackTasks().toMutableSet()
        if (enforced) {
            current.add(task)
        } else {
            current.remove(task)
        }
        saveEnforcedPlaybackTasks(current)
    }
}
