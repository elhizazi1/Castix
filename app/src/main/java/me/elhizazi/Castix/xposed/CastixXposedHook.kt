package me.elhizazi.Castix.xposed

import android.util.Log
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Entry point for LSPosed and LSPatch (assets/xposed_init).
 * NOTE: this class lives in the module ClassLoader, NOT in the app ClassLoader,
 * so it must never communicate with the UI through static fields. See XposedStatus.
 */
class CastixXposedHook : IXposedHookLoadPackage {

    companion object {
        private const val TAG = "CastixXposed"
        const val MY_PACKAGE_NAME = "me.elhizazi.Castix"
        val SUPPORTED_PACKAGES = HookTargets.SUPPORTED_PACKAGES
        private const val GRANTED = 1 // AudioManager.AUDIOFOCUS_REQUEST_GRANTED

        private fun detectLSPatch(lpparam: XC_LoadPackage.LoadPackageParam): Boolean {
            val byFactory = android.os.Build.VERSION.SDK_INT >= 28 && runCatching {
                lpparam.appInfo?.appComponentFactory?.contains("lspatch", ignoreCase = true) == true
            }.getOrDefault(false)
            if (byFactory) return true
            return listOf(
                "org.lsposed.lspatch.loader.LSPApplication",
                "org.lsposed.lspatch.metaloader.LSPAppComponentFactoryStub"
            ).any { name ->
                runCatching { Class.forName(name, false, lpparam.classLoader); true }.getOrDefault(false)
            }
        }
    }

    // Shared prefs written by the Castix UI. If unreadable (e.g. LSPatch) the hook stays enabled.
    private val prefs: XSharedPreferences? by lazy {
        runCatching {
            XSharedPreferences(MY_PACKAGE_NAME, HookConfig.PREFS_NAME).also { it.makeWorldReadable() }
        }.getOrNull()
    }

    private fun audioFocusHookEnabled(): Boolean = runCatching {
        val p = prefs ?: return true
        p.reload()
        p.getBoolean(HookConfig.KEY_AUDIO_FOCUS_HOOK, true)
    }.getOrDefault(true)

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        val pkg = lpparam?.packageName ?: return

        if (pkg == MY_PACKAGE_NAME) {
            // Make the app's OWN copy of XposedStatus report the truth (see XposedStatus.kt).
            val lsPatch = detectLSPatch(lpparam)
            try {
                XposedHelpers.findAndHookMethod(
                    "$MY_PACKAGE_NAME.xposed.XposedStatus", lpparam.classLoader, "isModuleActive",
                    XC_MethodReplacement.returnConstant(true)
                )
                XposedHelpers.findAndHookMethod(
                    "$MY_PACKAGE_NAME.xposed.XposedStatus", lpparam.classLoader, "isLSPatchRuntime",
                    XC_MethodReplacement.returnConstant(lsPatch)
                )
                Log.i(TAG, "Module active in Castix process (LSPatch=$lsPatch)")
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to publish module status to Castix UI", t)
            }
            return
        }

        if (pkg !in SUPPORTED_PACKAGES) return
        Log.i(TAG, "Xposed entrypoint invoked in target package: $pkg")

        // 1) Observe requestAudioFocus (diagnostics only)
        runCatching {
            XposedHelpers.findAndHookMethod(
                android.media.AudioManager::class.java, "requestAudioFocus",
                android.media.AudioManager.OnAudioFocusChangeListener::class.java,
                Int::class.javaPrimitiveType, Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        Log.i(TAG, "requestAudioFocus in $pkg result=${param.getResult()}")
                    }
                }
            )
        }.onFailure { Log.e(TAG, "requestAudioFocus hook failed in $pkg", it) }

        // 2) Block the app from abandoning audio focus (keeps playback alive in background)
        runCatching {
            XposedHelpers.findAndHookMethod(
                android.media.AudioManager::class.java, "abandonAudioFocus",
                android.media.AudioManager.OnAudioFocusChangeListener::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (audioFocusHookEnabled()) param.setResult(GRANTED)
                    }
                }
            )
            Log.i(TAG, "abandonAudioFocus hook registered for $pkg")
        }.onFailure { Log.e(TAG, "abandonAudioFocus hook failed in $pkg", it) }

        runCatching {
            XposedHelpers.findAndHookMethod(
                android.media.AudioManager::class.java, "abandonAudioFocusRequest",
                android.media.AudioFocusRequest::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (audioFocusHookEnabled()) param.setResult(GRANTED)
                    }
                }
            )
        }.onFailure { Log.e(TAG, "abandonAudioFocusRequest hook failed in $pkg", it) }
    }
}
