package me.elhizazi.Castix.privilege.domain.engine

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.os.SystemClock
import me.elhizazi.Castix.privilege.domain.model.EngineHealthStatus
import me.elhizazi.Castix.privilege.domain.model.EngineType
import me.elhizazi.Castix.privilege.domain.model.ExecutionResult
import me.elhizazi.Castix.privilege.domain.model.PlaybackOptimizationTask
import me.elhizazi.Castix.privilege.domain.model.ServiceHealth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * LSPosed / Xposed Framework Hooking Engine (Supplementary Engine):
 * Operates at the Zygote/ART runtime level to hook framework methods (e.g. AudioManager,
 * AudioPolicyService, MediaSessionService) without shell or binder IPC overhead.
 */
class LSPosedEngine : IPrivilegeEngine {

    override val engineType: EngineType = EngineType.LSPOSED
    override val displayName: String = "LSPosed (Framework Hook)"
    override val description: String = "Supplementary runtime hooking engine for in-process audio focus & battery spoofing."

    companion object {
        // Only these packages are LSPosed/EdXposed managers. Root managers are
        // intentionally kept separate: having Magisk/KernelSU/APatch installed
        // does NOT mean that LSPosed itself is installed.
        val LSPOSED_PACKAGES = listOf(
            "org.lsposed.manager",
            "io.github.lsposed.manager",
            "org.lsposed.manager.mod",
            "org.lsposed.manager.release",
            "com.jingmatrix.lsposed",
            "io.github.mywalkb.lsposed",
            "org.meowcat.edxposed.manager"
        )

        const val LSPOSED_ACTION_MANAGER = "org.lsposed.manager.ACTION_MANAGER"

        /** Manager identity is independent from the Xposed runtime and module activation. */
        fun getDetectedManagerVersion(context: Context): String? {
            val pm = context.packageManager
            for (pkg in LSPOSED_PACKAGES) {
                val info = runCatching { pm.getPackageInfo(pkg, 0) }.getOrNull()
                if (info != null) return "${info.versionName ?: "unknown"} ($pkg)"
                if (isPackageInstalled(pm, pkg)) return "installed ($pkg; version hidden)"
            }
            return null
        }

        fun getDetectedManagerName(context: Context): String? {
            val pm = context.packageManager
            if (isPackageInstalled(pm, "org.lsposed.manager") || isPackageInstalled(pm, "io.github.lsposed.manager") || isPackageInstalled(pm, "org.lsposed.manager.release")) {
                return "LSPosed Manager"
            }
            if (isPackageInstalled(pm, "org.lsposed.manager.mod") || isPackageInstalled(pm, "io.github.mywalkb.lsposed")) {
                return "LSPosed Mod Manager"
            }
            if (isPackageInstalled(pm, "com.jingmatrix.lsposed")) {
                return "LSPosed (Vector)"
            }
            if (isPackageInstalled(pm, "org.meowcat.edxposed.manager")) {
                return "EdXposed Manager"
            }
            // Root managers are deliberately not reported as an LSPosed manager.
            // Their presence alone must not make the LSPosed engine look installed.
            if (checkLSPosedRootFiles()) {
                return "LSPosed (Zygisk Daemon)"
            }
            return null
        }

        private fun isPackageInstalled(pm: PackageManager, packageName: String): Boolean {
            if (runCatching { pm.getPackageInfo(packageName, 0) }.isSuccess) return true
            // LSPosed forks may hide their manager from ordinary PackageManager queries.
            // A successful root package query is evidence of installation, not activation.
            return rootShell("pm path $packageName").startsWith("package:")
        }

        private fun rootShell(command: String): String = runCatching {
            val process = ProcessBuilder("su", "-c", command).redirectErrorStream(true).start()
            if (!process.waitFor(3, java.util.concurrent.TimeUnit.SECONDS)) {
                process.destroyForcibly()
                return@runCatching ""
            }
            if (process.exitValue() != 0) return@runCatching ""
            process.inputStream.bufferedReader().use { it.readText().trim() }
        }.getOrDefault("")

        private fun launchViaRoot(packageName: String): Boolean {
            // `monkey` resolves the fork's launcher through the system package manager,
            // including managers that hide their launcher from third-party apps.
            val output = rootShell("monkey -p $packageName -c android.intent.category.LAUNCHER 1")
            return output.contains("Events injected: 1")
        }

        private fun checkLSPosedRootFiles(): Boolean {
            val paths = listOf(
                "/data/adb/lspd",
                "/data/adb/modules/zygisk_lsposed",
                "/data/adb/modules/lsposed",
                "/data/adb/modules_update/zygisk_lsposed"
            )
            if (paths.any { java.io.File(it).exists() }) return true

            return runCatching {
                val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "test -d /data/adb/lspd && echo 1"))
                val output = process.inputStream.bufferedReader().readText().trim()
                output == "1"
            }.getOrDefault(false)
        }

        /**
         * TRUE only if the framework hooked XposedStatus.isModuleActive() inside THIS process.
         * (A static flag set from the module would be written to a different ClassLoader copy.)
         */
        val isModuleActivatedInScope: Boolean
            get() = runCatching { me.elhizazi.Castix.xposed.XposedStatus.isModuleActive() }.getOrDefault(false)
    }

    override suspend fun isAvailable(context: Context): Boolean = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val lsposedManagerFound = LSPOSED_PACKAGES.any { pkg ->
            isPackageInstalled(pm, pkg)
        }

        // Castix bundles the legacy Xposed API interfaces as compile-time stubs,
        // so Class.forName("de.robv.android.xposed.XposedBridge") is NOT evidence
        // that an Xposed/LSPosed runtime exists on the device.
        val rootDaemonFound = checkLSPosedRootFiles()

        lsposedManagerFound || rootDaemonFound || isModuleActivatedInScope
    }

    override suspend fun checkPermission(context: Context): Boolean = withContext(Dispatchers.IO) {
        isModuleActivatedInScope
    }

    override suspend fun requestPermission(context: Context): Result<Boolean> = withContext(Dispatchers.Main) {
        runCatching {
            val pm = context.packageManager

            // 1. Prefer the installed manager's own exported launcher, never a different fork.
            // The manager can be installed without an exported launcher on some builds.
            for (pkg in LSPOSED_PACKAGES) {
                if (!isPackageInstalled(pm, pkg)) continue
                val launchIntent = pm.getLaunchIntentForPackage(pkg)
                if (launchIntent != null && runCatching {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                }.isSuccess) return@runCatching true
            }

            // 2. Explicitly resolve launcher activities belonging to the installed fork.
            // Some forks have no conventional getLaunchIntentForPackage result.
            for (pkg in LSPOSED_PACKAGES) {
                if (!isPackageInstalled(pm, pkg)) continue
                val query = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    setPackage(pkg)
                }
                @Suppress("DEPRECATION")
                val activities = pm.queryIntentActivities(query, 0)
                for (activity in activities) {
                    val component = android.content.ComponentName(
                        activity.activityInfo.packageName, activity.activityInfo.name
                    )
                    val explicit = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_LAUNCHER)
                        setComponent(component)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    if (runCatching { context.startActivity(explicit) }.isSuccess) return@runCatching true
                }
            }

            // 3. Root fallback for hidden LSPosed manager activities.
            // Do not claim success unless the system launcher actually reports an event.
            for (pkg in LSPOSED_PACKAGES) {
                if (isPackageInstalled(pm, pkg) && launchViaRoot(pkg)) return@runCatching true
            }

            // 4. The legacy manager action may be exposed by some forks.
            val actionIntent = Intent(LSPOSED_ACTION_MANAGER).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (runCatching { context.startActivity(actionIntent) }.isSuccess) return@runCatching true

            // 5. Last resort: open the installed manager's system details rather
            // than silently doing nothing. This does not claim to activate hooks.
            for (pkg in LSPOSED_PACKAGES) {
                if (!isPackageInstalled(pm, pkg)) continue
                val details = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$pkg")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (runCatching { context.startActivity(details) }.isSuccess) return@runCatching true
            }

            // 4. If no LSPosed manager was found, explain cleanly without any dialer code or browser redirection
            throw IllegalStateException("لم يتم العثور على مدير LSPosed مثبت. يرجى تفعيل الموديول من خلال Zygisk (KernelSU / Magisk / APatch).")
        }
    }

    fun setSimulatedModuleActive(active: Boolean) {
        // Manual UI toggles must never masquerade as real LSPosed activation.
    }

    private fun unsupported(command: String) = ExecutionResult(
        command = command,
        exitCode = 1,
        stdout = "",
        stderr = "LSPosed hooks cannot be dispatched by a shell command. A verified, scoped Xposed module implementation is required.",
        executionTimeMs = 0L,
        engineUsed = engineType,
        isSuccess = false
    )

    override suspend fun executeCommand(command: String): ExecutionResult = unsupported(command)

    override suspend fun executePlaybackTask(
        task: PlaybackOptimizationTask,
        targetPackage: String
    ): ExecutionResult = unsupported("lsposed::${task.name} --pkg=$targetPackage")

    override suspend fun getHealth(context: Context): ServiceHealth = withContext(Dispatchers.IO) {
        val active = isModuleActivatedInScope
        val available = isAvailable(context)
        val managerName = getDetectedManagerName(context) ?: "LSPosed Manager"
        val managerVersion = getDetectedManagerVersion(context)

        when {
            active -> {
                ServiceHealth(
                    type = EngineType.LSPOSED,
                    status = EngineHealthStatus.ACTIVE,
                    message = "تم تحميل موديول Castix داخل عملية التطبيق",
                    version = managerVersion,
                    isUsable = true,
                    details = "تم تنفيذ نقطة دخول Xposed داخل Castix. يلزم التحقق من تحميلها داخل التطبيقات المستهدفة قبل تأكيد عمل Hooks الصوت."
                )
            }
            available -> {
                ServiceHealth(
                    type = EngineType.LSPOSED,
                    status = EngineHealthStatus.INSTALLED_INACTIVE,
                    message = "$managerName متاح (بانتظار التفعيل)",
                    version = managerVersion,
                    isUsable = false,
                    details = "تم اكتشاف مدير LSPosed، لكن Castix لم يُحمَّل بعد داخل عمليته. في LSPosed: فعّل وحدة Castix وحدّد Castix نفسه ضمن النطاق (Scope) مع التطبيقات المستهدفة، ثم أوقف Castix بالقوة (Force stop) وافتحه من جديد."
                )
            }
            else -> {
                ServiceHealth(
                    type = EngineType.LSPOSED,
                    status = EngineHealthStatus.NOT_INSTALLED,
                    message = "إطار LSPosed / Zygisk غير مثبت",
                    version = null,
                    isUsable = false,
                    details = "محرك تكميلي لأنظمة الروت (Zygisk / KernelSU / Magisk / APatch)."
                )
            }
        }
    }
}
