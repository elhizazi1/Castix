package me.elhizazi.Castix.themes

import android.content.Context
import android.os.BatteryManager
import org.json.JSONObject
import me.elhizazi.Castix.privilege.ui.AppSettingsManager
import me.elhizazi.Castix.privilege.ui.AppStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal object ThemeData {
    fun snapshot(context: Context, sessionStart: Long = 0L, foreground: String? = null, preview: Boolean = false): JSONObject {
        val s = AppSettingsManager.getInstance(context).settings.value
        val locale = Locale.forLanguageTag(AppSettingsManager.getEffectiveLanguage(s.language))
        val now = Date()
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY).takeIf { it in 0..100 }
        val app = foreground?.takeIf { it != context.packageName }?.let { pkg -> runCatching { context.packageManager.getApplicationLabel(context.packageManager.getApplicationInfo(pkg, 0)).toString() }.getOrNull() }
        return JSONObject().apply {
            put("preview", preview)
            put("time", SimpleDateFormat(if (android.text.format.DateFormat.is24HourFormat(context)) "HH:mm" else "hh:mm a", locale).format(now))
            put("timestamp", now.time)
            put("date", if (s.showDateOnBlackScreen) SimpleDateFormat("EEE, d MMM", locale).format(now) else JSONObject.NULL)
            put("battery", if (s.showBatteryOnBlackScreen) level ?: JSONObject.NULL else JSONObject.NULL)
            put("charging", if (s.showBatteryOnBlackScreen) bm.isCharging else JSONObject.NULL)
            put("notificationCount", if (s.showNotifications) ThemeNotifications.count ?: (if (preview) 3 else JSONObject.NULL) else JSONObject.NULL)
            put("foregroundApp", if (s.showAppBadgeOnBlackScreen) app ?: (if (preview) "YouTube" else AppStrings.getForContext(context, "black_screen_continuous_play")) else JSONObject.NULL)
            put("sessionMinutes", if (s.showPlaybackTimer) { if (preview) 25L else if (sessionStart > 0) ((now.time-sessionStart)/60000).coerceAtLeast(0) else JSONObject.NULL } else JSONObject.NULL)
            put("sessionLabel", if (s.showPlaybackTimer) AppStrings.getForContext(context, "black_screen_session_timer", if (preview) 25L else ((now.time-sessionStart)/60000).coerceAtLeast(0)) else JSONObject.NULL)
            put("inscription", s.customInscription)
            put("accentColor", s.accentColorHex)
            put("language", locale.toLanguageTag())
            put("scale", s.clockScalePercent)
            put("verticalOffset", s.clockVerticalOffsetDp)
            put("spacing", s.clockSpacingPercent)
        }
    }
}
