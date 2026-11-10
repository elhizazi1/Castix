package me.elhizazi.Castix.privilege.domain.engine

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import me.elhizazi.Castix.privilege.domain.model.EngineHealthStatus
import me.elhizazi.Castix.privilege.domain.model.EngineType
import me.elhizazi.Castix.privilege.domain.model.ExecutionResult
import me.elhizazi.Castix.privilege.domain.model.PlaybackOptimizationTask
import me.elhizazi.Castix.privilege.domain.model.ServiceHealth
import me.elhizazi.Castix.xposed.HookTargets
import me.elhizazi.Castix.xposed.XposedStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * LSPatch Engine (rootless Xposed).
 * LSPatch works by patching the TARGET apk (YouTube, ...) and embedding the Castix module in it.
 * Castix cannot patch apps itself, but it CAN detect which installed apps were patched:
 * a patched APK uses an LSPatch appComponentFactory.
 */
class LSPatchEngine : IPrivilegeEngine {

    override val engineType: EngineType = EngineType.LSPATCH
    override val displayName: String = "LSPatch (Rootless)"
    override val description: String = "Rootless Xposed engine injecting audio bypass hooks directly into target APKs without root."

    companion object {
        const val LSPATCH_PACKAGE = "org.lsposed.lspatch"

        /** TRUE only if Castix itself is running inside an LSPatch-patched APK (hooked handshake). */
        val isModulePatchedActive: Boolean
            get() = runCatching { XposedStatus.isModuleActive() && XposedStatus.isLSPatchRuntime() }
                .getOrDefault(false)

        private fun isLSPatched(info: ApplicationInfo): Boolean {
            val factory = if (android.os.Build.VERSION.SDK_INT >= 28) runCatching { info.appComponentFactory }.getOrNull() else null
            if (factory?.contains("lspatch", ignoreCase = true) == true) return true
            return runCatching { info.metaData?.containsKey("lspatch") == true }.getOrDefault(false)
        }

        /** Installed target apps that have been patched with LSPatch. */
        fun findPatchedTargets(context: Context): List<String> {
            val pm = context.packageManager
            return HookTargets.SUPPORTED_PACKAGES
                .filter { pkg ->
                    runCatching {
                        isLSPatched(pm.getApplicationInfo(pkg, PackageManager.GET_META_DATA))
                    }.getOrDefault(false)
                }
        }
    }

    private fun managerInstalled(context: Context): Boolean =
        runCatching { context.packageManager.getPackageInfo(LSPATCH_PACKAGE, 0) }.isSuccess

    override suspend fun isAvailable(context: Context): Boolean = withContext(Dispatchers.IO) {
        managerInstalled(context) || isModulePatchedActive || findPatchedTargets(context).isNotEmpty()
    }

    override suspend fun checkPermission(context: Context): Boolean = withContext(Dispatchers.IO) {
        isModulePatchedActive || findPatchedTargets(context).isNotEmpty()
    }

    override suspend fun requestPermission(context: Context): Result<Boolean> = withContext(Dispatchers.Main) {
        runCatching {
            val intent = context.packageManager.getLaunchIntentForPackage(LSPATCH_PACKAGE)
                ?: throw IllegalStateException("مدير LSPatch غير مثبت.")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            // Opening the manager is not confirmation; status comes from detected patched apps.
            checkPermission(context)
        }
    }

    private fun unsupported(command: String): ExecutionResult = ExecutionResult(
        engineUsed = engineType,
        command = command,
        exitCode = 1,
        stdout = "",
        stderr = "LSPatch cannot execute this operation from Castix. Patch the target app in LSPatch Manager with the Castix module embedded.",
        isSuccess = false,
        executionTimeMs = 0L
    )

    override suspend fun executeCommand(command: String): ExecutionResult = unsupported(command)

    override suspend fun executePlaybackTask(
        task: PlaybackOptimizationTask,
        targetPackage: String
    ): ExecutionResult = unsupported("lspatch::${task.name} --pkg=$targetPackage")

    override suspend fun getHealth(context: Context): ServiceHealth = withContext(Dispatchers.IO) {
        val manager = managerInstalled(context)
        val selfPatched = isModulePatchedActive
        val patched = findPatchedTargets(context)

        when {
            selfPatched || patched.isNotEmpty() -> ServiceHealth(
                type = engineType,
                status = EngineHealthStatus.ACTIVE,
                message = if (selfPatched) "Castix يعمل داخل APK مُرقَّع بـ LSPatch"
                          else "تم رصد ${patched.size} تطبيق مُرقَّع بـ LSPatch",
                isUsable = true,
                details = if (selfPatched) "تم تحميل الموديول داخل عملية Castix عبر LSPatch."
                          else "تطبيقات مُرقَّعة: ${patched.joinToString()}. تأكد أن موديول Castix مُضمَّن فيها عند الترقيع."
            )
            manager -> ServiceHealth(
                type = engineType,
                status = EngineHealthStatus.INSTALLED_INACTIVE,
                message = "مدير LSPatch مثبت؛ لا يوجد تطبيق مُرقَّع بعد",
                isUsable = false,
                details = "في LSPatch: اختر Patch → اختر APK التطبيق المستهدف (مثل YouTube) → فعّل Embed modules واختر Castix → ثبّت النسخة المُرقَّعة بعد حذف الأصلية."
            )
            else -> ServiceHealth(
                type = engineType,
                status = EngineHealthStatus.NOT_INSTALLED,
                message = "LSPatch غير متوفر",
                isUsable = false,
                details = "ثبّت LSPatch Manager أو شغّل Castix من APK مُرقَّع."
            )
        }
    }
}
