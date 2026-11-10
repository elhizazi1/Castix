package me.elhizazi.Castix.xposed

import android.util.Log
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import me.elhizazi.Castix.privilege.domain.engine.LSPosedEngine
import me.elhizazi.Castix.privilege.domain.engine.LSPatchEngine

/**
 * Entry point for LSPosed and LSPatch frameworks.
 * Recognized natively via assets/xposed_init and AndroidManifest xposedmodule metadata.
 */
class CastixXposedHook : IXposedHookLoadPackage {

    companion object {
        private const val TAG = "CastixXposed"
        const val MY_PACKAGE_NAME = "me.elhizazi.Castix"
        val SUPPORTED_PACKAGES = setOf(
            MY_PACKAGE_NAME,
            "com.google.android.youtube",
            "com.google.android.apps.youtube.music",
            "com.facebook.katana",
            "com.instagram.android",
            "com.twitter.android",
            "com.instagram.barcelona",
            "com.android.chrome",
            "org.mozilla.firefox",
            "com.spotify.music",
            "com.soundcloud.android",
            "com.netflix.mediaclient"
        )
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        val pkg = lpparam?.packageName ?: return
        val processName = lpparam.processName ?: ""

        // Detect the actual runtime framework before reporting an engine as active.
        if (pkg == MY_PACKAGE_NAME || processName.contains(MY_PACKAGE_NAME)) {
            val isLSPatchRuntime = runCatching {
                Class.forName("org.lsposed.lspatch.Loader", false, lpparam.classLoader)
                true
            }.getOrDefault(false)

            if (isLSPatchRuntime) {
                Log.i(TAG, "[Castix] Loaded inside Castix process through LSPatch.")
                LSPatchEngine.isModulePatchedActive = true
            } else {
                Log.i(TAG, "[Castix] Loaded inside Castix process through LSPosed.")
                LSPosedEngine.isModuleActivatedInScope = true
            }
            return
        }

        if (pkg !in SUPPORTED_PACKAGES && pkg != "android") {
            return
        }

        Log.i(TAG, "[Castix] Xposed entrypoint invoked in target package: $pkg")

        try {
            XposedHelpers.findAndHookMethod(
                android.media.AudioManager::class.java, "requestAudioFocus",
                android.media.AudioManager.OnAudioFocusChangeListener::class.java,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        Log.i(TAG, "Audio focus request observed in $pkg; result=${param.getResult()}")
                    }
                }
            )
            Log.i(TAG, "AudioManager requestAudioFocus hook registered for $pkg")
        } catch (t: Throwable) {
            Log.e(TAG, "Audio focus hook registration failed in $pkg", t)
        }
    }
}
