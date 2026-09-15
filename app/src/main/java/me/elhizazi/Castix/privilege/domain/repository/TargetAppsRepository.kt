package me.elhizazi.Castix.privilege.domain.repository

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import me.elhizazi.Castix.privilege.domain.model.FloatingButtonMode
import me.elhizazi.Castix.privilege.domain.model.TargetApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository responsible for managing the user's selected target applications,
 * persisting whitelist preferences, and enumerating installed media/browser apps.
 */
class TargetAppsRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("castix_target_apps_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SELECTED_PACKAGES = "key_selected_packages"
        private const val KEY_FLOATING_ENABLED = "key_floating_enabled"
        private const val KEY_FLOATING_MODE = "key_floating_mode"

        val DEFAULT_TARGET_APPS = listOf(
            TargetApp("com.google.android.youtube", "YouTube", isSelected = true, isMediaApp = true),
            TargetApp("com.google.android.apps.youtube.music", "YouTube Music", isSelected = true, isMediaApp = true),
            TargetApp("com.facebook.katana", "Facebook", isSelected = true, isMediaApp = true),
            TargetApp("com.instagram.android", "Instagram", isSelected = true, isMediaApp = true),
            TargetApp("com.twitter.android", "Twitter / X", isSelected = true, isMediaApp = true),
            TargetApp("com.instagram.barcelona", "Threads", isSelected = true, isMediaApp = true),
            TargetApp("com.android.chrome", "Google Chrome", isSelected = true, isMediaApp = true),
            TargetApp("com.spotify.music", "Spotify", isSelected = true, isMediaApp = true),
            TargetApp("com.soundcloud.android", "SoundCloud", isSelected = false, isMediaApp = true),
            TargetApp("org.schabi.newpipe", "NewPipe", isSelected = false, isMediaApp = true),
            TargetApp("com.brave.browser", "Brave Browser", isSelected = false, isMediaApp = true),
            TargetApp("org.mozilla.firefox", "Firefox", isSelected = false, isMediaApp = true),
            TargetApp("tv.twitch.android.app", "Twitch", isSelected = false, isMediaApp = true)
        )
    }

    fun getSelectedPackages(): Set<String> {
        val defaultSet = DEFAULT_TARGET_APPS.filter { it.isSelected }.map { it.packageName }.toSet()
        return prefs.getStringSet(KEY_SELECTED_PACKAGES, defaultSet) ?: defaultSet
    }

    fun saveSelectedPackages(packages: Set<String>) {
        prefs.edit().putStringSet(KEY_SELECTED_PACKAGES, packages).apply()
    }

    fun isFloatingButtonEnabled(): Boolean {
        return prefs.getBoolean(KEY_FLOATING_ENABLED, true)
    }

    fun setFloatingButtonEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FLOATING_ENABLED, enabled).apply()
    }

    fun getFloatingButtonMode(): FloatingButtonMode {
        val name = prefs.getString(KEY_FLOATING_MODE, FloatingButtonMode.ONLY_SELECTED_APPS.name)
        return try {
            FloatingButtonMode.valueOf(name ?: FloatingButtonMode.ONLY_SELECTED_APPS.name)
        } catch (_: Exception) {
            FloatingButtonMode.ONLY_SELECTED_APPS
        }
    }

    fun setFloatingButtonMode(mode: FloatingButtonMode) {
        prefs.edit().putString(KEY_FLOATING_MODE, mode.name).apply()
    }

    /**
     * Loads installed applications combined with default media apps and user selections.
     */
    suspend fun getCombinedTargetApps(): List<TargetApp> = withContext(Dispatchers.IO) {
        val selected = getSelectedPackages()
        val pm = context.packageManager
        val installedAppsMap = mutableMapOf<String, TargetApp>()

        // 1. Put defaults first
        for (defaultApp in DEFAULT_TARGET_APPS) {
            installedAppsMap[defaultApp.packageName] = defaultApp.copy(
                isSelected = selected.contains(defaultApp.packageName)
            )
        }

        // 2. Query installed apps with launcher intents
        try {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
            for (info in resolveInfos) {
                val pkg = info.activityInfo.packageName
                if (pkg == context.packageName) continue // skip self

                val appName = info.loadLabel(pm).toString()
                val isMedia = pkg.contains("music", ignoreCase = true) ||
                        pkg.contains("video", ignoreCase = true) ||
                        pkg.contains("player", ignoreCase = true) ||
                        pkg.contains("media", ignoreCase = true) ||
                        pkg.contains("browser", ignoreCase = true) ||
                        pkg.contains("chrome", ignoreCase = true)

                val existing = installedAppsMap[pkg]
                if (existing != null) {
                    installedAppsMap[pkg] = existing.copy(appName = appName)
                } else {
                    installedAppsMap[pkg] = TargetApp(
                        packageName = pkg,
                        appName = appName,
                        isSelected = selected.contains(pkg),
                        isMediaApp = isMedia
                    )
                }
            }
        } catch (_: Exception) {
            // Fallback gracefully on query limits
        }

        installedAppsMap.values.sortedWith(
            compareByDescending<TargetApp> { it.isSelected }
                .thenByDescending { it.isMediaApp }
                .thenBy { it.appName.lowercase() }
        )
    }
}
