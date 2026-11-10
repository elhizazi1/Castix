package me.elhizazi.Castix.xposed

import android.content.Context
import android.util.Log
import java.io.File

/**
 * Settings shared between the Castix UI and the hooks running inside other apps.
 * The hook side reads them with XSharedPreferences (see CastixXposedHook).
 * Default = enabled.
 */
object HookConfig {
    const val PREFS_NAME = "castix_hook_prefs"
    const val KEY_AUDIO_FOCUS_HOOK = "audio_focus_hook_enabled"
    private const val TAG = "CastixHookConfig"

    @Suppress("DEPRECATION", "WorldReadableFiles")
    private fun prefs(context: Context) = try {
        // LSPosed allows MODE_WORLD_READABLE for the module's own prefs when the module is active.
        context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_READABLE)
    } catch (_: SecurityException) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isAudioFocusHookEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_AUDIO_FOCUS_HOOK, true)

    fun setAudioFocusHookEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_AUDIO_FOCUS_HOOK, enabled).commit()
        // Best effort: make the file readable by the hooked processes.
        runCatching {
            val dir = File(context.applicationInfo.dataDir)
            dir.setExecutable(true, false)
            val prefDir = File(dir, "shared_prefs").also { it.setExecutable(true, false) }
            File(prefDir, "$PREFS_NAME.xml").setReadable(true, false)
        }.onFailure { Log.w(TAG, "could not relax prefs permissions", it) }
    }
}
