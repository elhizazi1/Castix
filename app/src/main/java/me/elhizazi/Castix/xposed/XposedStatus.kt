package me.elhizazi.Castix.xposed

/**
 * Handshake between the Xposed hook and the Castix UI.
 *
 * WHY THIS EXISTS: LSPosed/LSPatch load the module APK in a SEPARATE ClassLoader.
 * Setting a static flag from CastixXposedHook therefore changes a *different copy*
 * of the class than the one the UI reads, so the flag was always false.
 *
 * Instead, the hook hooks THESE methods inside the app's own ClassLoader
 * (lpparam.classLoader) and makes them return true. Only the framework can do that,
 * so a `true` result is real proof the module was loaded into this process.
 *
 * Must not be inlined / renamed / removed by R8 (see proguard-rules.pro).
 */
object XposedStatus {
    /** Replaced by the hook with `true` when Castix is loaded by LSPosed or LSPatch. */
    @JvmStatic
    fun isModuleActive(): Boolean = false

    /** Replaced by the hook with `true` when the framework in this process is LSPatch. */
    @JvmStatic
    fun isLSPatchRuntime(): Boolean = false
}
