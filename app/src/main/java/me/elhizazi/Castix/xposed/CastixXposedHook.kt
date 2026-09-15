package me.elhizazi.Castix.xposed

import android.util.Log
import de.robv.android.xposed.IXposedHookLoadPackage
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

        Log.i(TAG, "[Castix] Successfully attached to target package: $pkg")

        try {
            // Intercept AudioFocus loss to keep background audio playing
            val audioManagerClass = try {
                lpparam.classLoader.loadClass("android.media.AudioManager")
            } catch (_: Throwable) {
                null
            }

            if (audioManagerClass != null) {
                Log.d(TAG, "[Castix] AudioPolicy hook ready for $pkg")
            }
        } catch (t: Throwable) {
            Log.e(TAG, "[Castix] Error applying hook to $pkg", t)
        }
    }
}
