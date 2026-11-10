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

/**
 * LSPatch Engine (Rootless Xposed / Sandboxed In-App Dex Hooking):
 * Enables non-rooted users to use Xposed module hooks directly within target applications
 * by embedding a dex loader (such as Shizuku/LSPatch loader or patched APKs).
 */
class LSPatchEngine : IPrivilegeEngine {

    override val engineType: EngineType = EngineType.LSPATCH
    override val displayName: String = "LSPatch (Rootless)"
    override val description: String = "Rootless Xposed engine injecting audio bypass hooks directly into target APKs without root."

    companion object {
        const val LSPATCH_PACKAGE = "org.lsposed.lspatch"

        @Volatile
        var isModulePatchedActive: Boolean = false
    }

    override suspend fun isAvailable(context: Context): Boolean = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val managerFound = runCatching { pm.getPackageInfo(LSPATCH_PACKAGE, 0) }.isSuccess

        val loaderFound = runCatching {
            Class.forName("org.lsposed.lspatch.Loader")
            true
        }.getOrDefault(false)


        // Check if installed or running in a sandboxed patched container
        managerFound || loaderFound
    }

    override suspend fun checkPermission(context: Context): Boolean = withContext(Dispatchers.IO) {
        false // Manager installation does not prove a module is active in another app.
    }

    override suspend fun requestPermission(context: Context): Result<Boolean> = withContext(Dispatchers.Main) {
        runCatching {
            val pm = context.packageManager
            val intent = pm.getLaunchIntentForPackage(LSPATCH_PACKAGE)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
            false // Opening the manager is not permission confirmation.
        }
    }

    // LSPatch is an external APK patching tool, not an in-process shell executor.
    // Never report successful patching or injection without a verified integration.
    private fun unsupported(command: String): ExecutionResult = ExecutionResult(
        engineUsed = engineType,
        command = command,
        exitCode = 1,
        stdout = "",
        stderr = "LSPatch cannot execute this operation from Castix. Configure and verify the patched APK and module in LSPatch Manager; no hook was injected.",
        isSuccess = false,
        executionTimeMs = 0L
    )

    override suspend fun executeCommand(command: String): ExecutionResult = unsupported(command)

    override suspend fun executePlaybackTask(
        task: PlaybackOptimizationTask,
        targetPackage: String
    ): ExecutionResult = unsupported("lspatch::${task.name} --pkg=$targetPackage")

    override suspend fun getHealth(context: Context): ServiceHealth = withContext(Dispatchers.IO) {
        val available = isAvailable(context)
        val permGranted = false

        val status = when {
            available -> EngineHealthStatus.INSTALLED_INACTIVE
            else -> EngineHealthStatus.NOT_INSTALLED
        }

        val isActuallyUsable = false

        ServiceHealth(
            type = engineType,
            status = status,
            message = if (available) "مدير LSPatch مثبت؛ حالة ترقيع التطبيقات غير مؤكدة" else "LSPatch غير متوفر",
            version = null,
            isUsable = isActuallyUsable,
            details = if (available) {
                "Manager detected only. Castix cannot verify patched APKs or injected modules; automatic patching is not implemented."
            } else {
                "LSPatch not detected. Install LSPatch manager or run within an LSPatch-patched APK."
            }
        )
    }
}
