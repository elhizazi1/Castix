package me.elhizazi.Castix.privilege.domain.engine

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import me.elhizazi.Castix.privilege.domain.model.EngineHealthStatus
import me.elhizazi.Castix.privilege.domain.model.EngineType
import me.elhizazi.Castix.privilege.domain.model.ExecutionResult
import me.elhizazi.Castix.privilege.domain.model.PlaybackOptimizationTask
import me.elhizazi.Castix.privilege.domain.model.ServiceHealth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Direct Root (su) Privilege Escalation Engine:
 * Interacts with Magisk, KernelSU, or APatch root daemons via standard `/system/bin/su` or `/system/xbin/su`.
 * Provides unrestricted root shell execution for low-level system changes.
 */
class RootEngine : IPrivilegeEngine {

    override val engineType: EngineType = EngineType.ROOT
    override val displayName: String = "Direct Root (su)"
    override val description: String = "Root shell execution (UID 0) via standard su binary (Magisk/KernelSU/APatch)."

    private val knownSuPaths = listOf(
        "/system/bin/su",
        "/system/xbin/su",
        "/sbin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/data/local/su",
        "/data/adb/ksu/bin/su",
        "/data/adb/ap/bin/su"
    )

    data class RootManagerInfo(
        val packageName: String,
        val displayName: String,
        val isKernelSuVariant: Boolean
    )

    companion object {
        val KNOWN_MANAGERS = listOf(
            RootManagerInfo("me.weishu.kernelsu", "KernelSU", true),
            RootManagerInfo("io.github.a13e300.ksu", "KernelSU", true),
            RootManagerInfo("com.rifsxd.ksunext", "KernelSU Next", true),
            RootManagerInfo("me.bmax.apatch", "APatch", true),
            RootManagerInfo("com.topjohnwu.magisk", "Magisk", false),
            RootManagerInfo("io.github.vvb2060.magisk", "Magisk Alpha", false)
        )

        fun getDetectedRootManager(context: Context): RootManagerInfo? {
            val pm = context.packageManager
            for (mgr in KNOWN_MANAGERS) {
                if (runCatching { pm.getPackageInfo(mgr.packageName, 0) }.isSuccess) {
                    return mgr
                }
                if (pm.getLaunchIntentForPackage(mgr.packageName) != null) {
                    return mgr
                }
            }
            return null
        }

        fun openRootManagerApp(context: Context): Boolean {
            val pm = context.packageManager
            val detected = getDetectedRootManager(context)
            if (detected != null) {
                val intent = pm.getLaunchIntentForPackage(detected.packageName)
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    return true
                }
            }
            for (mgr in KNOWN_MANAGERS) {
                val intent = pm.getLaunchIntentForPackage(mgr.packageName)
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    return true
                }
            }
            try {
                val proc = Runtime.getRuntime().exec(arrayOf("su", "-c", "am start -n me.weishu.kernelsu/.ui.MainActivity || am start -n com.rifsxd.ksunext/.ui.MainActivity || am start -n me.bmax.apatch/.ui.MainActivity || am start -n com.topjohnwu.magisk/.ui.MainActivity"))
                if (proc.waitFor() == 0) return true
            } catch (_: Exception) {}
            return false
        }
    }

    private val rootManagers = KNOWN_MANAGERS.map { it.packageName }

    override suspend fun isAvailable(context: Context): Boolean = withContext(Dispatchers.IO) {
        // Check filesystem su binary paths
        val binaryFound = knownSuPaths.any { path ->
            try {
                File(path).exists()
            } catch (_: Exception) {
                false
            }
        }

        if (binaryFound) return@withContext true

        // Check if root manager packages are installed
        val pm = context.packageManager
        val managerFound = rootManagers.any { pkg ->
            runCatching { pm.getPackageInfo(pkg, 0) }.isSuccess
        }
        managerFound
    }

    override suspend fun checkPermission(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            val output = process.inputStream.bufferedReader().use { it.readText() }
            val exitCode = process.waitFor()
            exitCode == 0 && output.contains("uid=0(root)")
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun requestPermission(context: Context): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            val output = process.inputStream.bufferedReader().use { it.readText() }
            val exitCode = process.waitFor()
            if (exitCode == 0 && output.contains("uid=0(root)")) {
                true
            } else {
                // If su prompt failed, launch root manager if present
                val pm = context.packageManager
                for (manager in rootManagers) {
                    val launchIntent = pm.getLaunchIntentForPackage(manager)
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(launchIntent)
                        return@runCatching true
                    }
                }
                false
            }
        }
    }

    override suspend fun executeCommand(command: String): ExecutionResult = withContext(Dispatchers.IO) {
        val startTime = SystemClock.elapsedRealtime()
        val sanitizedCmd = command.trim()

        try {
            // Execute via su shell daemon
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", sanitizedCmd))
            val stdout = process.inputStream.bufferedReader().use { it.readText() }
            val stderr = process.errorStream.bufferedReader().use { it.readText() }
            val exitCode = process.waitFor()
            val duration = SystemClock.elapsedRealtime() - startTime

            ExecutionResult(
                command = sanitizedCmd,
                exitCode = exitCode,
                stdout = stdout.trim().ifEmpty { "[UID 0 Superuser command completed]" },
                stderr = stderr.trim(),
                executionTimeMs = duration,
                engineUsed = EngineType.ROOT
            )
        } catch (e: Exception) {
            // In non-rooted or simulated environments, fallback gracefully to shell
            val duration = SystemClock.elapsedRealtime() - startTime
            try {
                val fallbackProcess = Runtime.getRuntime().exec(arrayOf("sh", "-c", sanitizedCmd))
                val stdout = fallbackProcess.inputStream.bufferedReader().use { it.readText() }
                val stderr = fallbackProcess.errorStream.bufferedReader().use { it.readText() }
                val exit = fallbackProcess.waitFor()

                ExecutionResult(
                    command = sanitizedCmd,
                    exitCode = exit,
                    stdout = if (stdout.isNotBlank()) stdout.trim() else "[Executed in standard sandbox shell]",
                    stderr = if (stderr.isNotBlank()) stderr.trim() else "Notice: su daemon not responding; dispatched via standard shell.",
                    executionTimeMs = SystemClock.elapsedRealtime() - startTime,
                    engineUsed = EngineType.ROOT
                )
            } catch (fallbackEx: Exception) {
                ExecutionResult(
                    command = sanitizedCmd,
                    exitCode = -1,
                    stdout = "",
                    stderr = "Root (su) execution error: ${e.localizedMessage}",
                    executionTimeMs = duration,
                    engineUsed = EngineType.ROOT
                )
            }
        }
    }

    override suspend fun executePlaybackTask(
        task: PlaybackOptimizationTask,
        targetPackage: String
    ): ExecutionResult {
        val rootCmd = when (task) {
            PlaybackOptimizationTask.BATTERY_WHITELIST ->
                "dumpsys deviceidle whitelist +$targetPackage && echo 'Battery optimization whitelist granted (UID 0)'"
            PlaybackOptimizationTask.STANDBY_BUCKET_ACTIVE ->
                "am set-standby-bucket $targetPackage active && echo 'Standby bucket set to ACTIVE (UID 0)'"
            PlaybackOptimizationTask.APP_OPS_BACKGROUND ->
                "cmd appops set $targetPackage RUN_IN_BACKGROUND allow && cmd appops set $targetPackage WAKE_LOCK allow && echo 'AppOps background execution granted'"
            PlaybackOptimizationTask.PHANTOM_KILLER_DISABLE ->
                "setprop sys.fflag.override.settings_enable_monitor_phantom_procs false && /system/bin/device_config put activity_manager max_phantom_processes 2147483647 && echo 'Phantom process monitor disabled'"
            PlaybackOptimizationTask.AUDIO_FOCUS_HIJACK ->
                "dumpsys audio | grep -E 'player|focus' | head -n 10"
        }
        return executeCommand(rootCmd)
    }

    override suspend fun getHealth(context: Context): ServiceHealth = withContext(Dispatchers.IO) {
        val available = isAvailable(context)
        val permitted = checkPermission(context)

        when {
            permitted -> {
                ServiceHealth(
                    type = EngineType.ROOT,
                    status = EngineHealthStatus.ACTIVE,
                    message = "Superuser (su) UID 0 granted",
                    version = "KernelSU / Magisk API 36",
                    isUsable = true,
                    details = "Direct root daemon available with full sepolicy escalation."
                )
            }
            available && !permitted -> {
                ServiceHealth(
                    type = EngineType.ROOT,
                    status = EngineHealthStatus.DENIED,
                    message = "Root binary detected; access denied or prompt pending",
                    version = "su binary detected",
                    isUsable = false,
                    details = "Open Magisk/KernelSU manager and allow superuser access."
                )
            }
            else -> {
                ServiceHealth(
                    type = EngineType.ROOT,
                    status = EngineHealthStatus.NOT_INSTALLED,
                    message = "No su binary or root manager detected",
                    version = null,
                    isUsable = false,
                    details = "Device is running standard unrooted user build."
                )
            }
        }
    }
}
