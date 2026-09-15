package me.elhizazi.Castix.privilege.domain.model

import java.util.UUID

/**
 * System privilege escalation engine backends supported by the application.
 */
enum class EngineType(
    val displayName: String,
    val shortName: String,
    val architecture: String,
    val securityScope: String,
    val description: String
) {
    SHIZUKU(
        displayName = "Shizuku Binder IPC",
        shortName = "Shizuku",
        architecture = "Remote Binder Service (adb/root)",
        securityScope = "User-authorized system APIs via IPC",
        description = "Direct Binder IPC via user-authorized Shizuku daemon (UID 2000 shell)."
    ),
    DHIZUKU(
        displayName = "Dhizuku Device Owner",
        shortName = "Dhizuku",
        architecture = "DevicePolicyManager Proxy",
        securityScope = "Device Owner policy delegations",
        description = "Device Owner delegation API for system-level policy & background exemptions."
    ),
    ROOT(
        displayName = "Direct Root (su)",
        shortName = "Root",
        architecture = "Linux setuid / su Shell daemon",
        securityScope = "Unrestricted superuser shell",
        description = "Root shell execution (UID 0) via standard su binary (Magisk/KernelSU/APatch)."
    ),
    LSPOSED(
        displayName = "LSPosed Hook Engine",
        shortName = "LSPosed",
        architecture = "Zygote ART Framework Injection",
        securityScope = "In-process memory & API hooking",
        description = "Supplementary runtime hooking engine for in-process audio focus & battery spoofing."
    ),
    LSPATCH(
        displayName = "LSPatch Rootless",
        shortName = "LSPatch",
        architecture = "Rootless APK Patching / In-App Dex Loader",
        securityScope = "Sandboxed app-level Xposed runtime",
        description = "Rootless Xposed engine injecting audio bypass hooks directly into target APKs without root."
    ),
    ACCESSIBILITY(
        displayName = "Android Accessibility",
        shortName = "Accessibility",
        architecture = "Android Accessibility Framework (Zero-Root)",
        securityScope = "System Setting Elevation & Keep-Alive",
        description = "Standard Android Accessibility privilege for real-time foreground app detection and system keep-alive."
    );

    fun getLocalizedName(lang: String): String {
        val key = when (this) {
            SHIZUKU -> "shizuku_name"
            DHIZUKU -> "dhizuku_name"
            ROOT -> "root_name"
            LSPOSED -> "lsposed_name"
            LSPATCH -> "lspatch_name"
            ACCESSIBILITY -> "accessibility_name"
        }
        return me.elhizazi.Castix.privilege.ui.AppStrings.get(key, lang)
    }

    fun getLocalizedDescription(lang: String): String {
        val key = when (this) {
            SHIZUKU -> "engine_desc_shizuku"
            DHIZUKU -> "engine_desc_dhizuku"
            ROOT -> "engine_desc_root"
            LSPOSED -> "engine_desc_lsposed"
            LSPATCH -> "engine_desc_lspatch"
            ACCESSIBILITY -> "engine_desc_accessibility"
        }
        return me.elhizazi.Castix.privilege.ui.AppStrings.get(key, lang)
    }
}

/**
 * Health and activation state of each engine.
 */
enum class EngineHealthStatus(val label: String) {
    ACTIVE("Active"),
    AVAILABLE("Available"),
    PERMISSION_REQUIRED("Perm Needed"),
    INSTALLED_INACTIVE("Inactive"),
    NOT_INSTALLED("Not Installed"),
    DENIED("Access Denied"),
    ERROR("Engine Error")
}

/**
 * Detailed health metrics for an engine instance.
 */
data class ServiceHealth(
    val type: EngineType,
    val status: EngineHealthStatus,
    val message: String,
    val version: String? = null,
    val isUsable: Boolean = false,
    val details: String = ""
)

/**
 * Standardized command execution result.
 */
data class ExecutionResult(
    val command: String,
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
    val executionTimeMs: Long,
    val engineUsed: EngineType,
    val isSuccess: Boolean = exitCode == 0
)

/**
 * Pre-configured Android 17 background playback privilege operations.
 */
enum class PlaybackOptimizationTask(
    val title: String,
    val description: String,
    val explanation: String
) {
    BATTERY_WHITELIST(
        title = "Doze & Battery Exemption",
        description = "dumpsys deviceidle whitelist +pkg",
        explanation = "Exempts playback service from Android 17 Doze and deep sleep throttles."
    ),
    STANDBY_BUCKET_ACTIVE(
        title = "Force Active Standby Bucket",
        description = "am set-standby-bucket <pkg> active",
        explanation = "Prevents Android OS from placing the player process into RESTRICTED or RARE buckets."
    ),
    APP_OPS_BACKGROUND(
        title = "Permit Run in Background",
        description = "cmd appops set <pkg> RUN_IN_BACKGROUND allow",
        explanation = "Bypasses system restrictions on background thread and binder execution."
    ),
    PHANTOM_KILLER_DISABLE(
        title = "Suppress Phantom Process Killer",
        description = "setprop sys.fflag.override.settings_enable_monitor_phantom_procs false",
        explanation = "Prevents system from aggressively killing child audio decoders & native threads."
    ),
    AUDIO_FOCUS_HIJACK(
        title = "LSPosed Audio Focus Override",
        description = "Hook AudioManager#abandonAudioFocus",
        explanation = "Forces media playback to maintain audio focus even during transient ducking interrupts."
    )
}

/**
 * Terminal console output model for live execution log displays.
 */
data class ConsoleEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val engine: EngineType,
    val command: String,
    val exitCode: Int,
    val output: String,
    val isError: Boolean = false,
    val durationMs: Long = 0L
)
