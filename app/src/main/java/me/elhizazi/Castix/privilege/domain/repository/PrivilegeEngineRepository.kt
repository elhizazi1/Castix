package me.elhizazi.Castix.privilege.domain.repository

import android.content.Context
import android.os.Build
import me.elhizazi.Castix.privilege.domain.engine.AccessibilityEngine
import me.elhizazi.Castix.privilege.domain.engine.DhizukuEngine
import me.elhizazi.Castix.privilege.domain.engine.IPrivilegeEngine
import me.elhizazi.Castix.privilege.domain.engine.LSPatchEngine
import me.elhizazi.Castix.privilege.domain.engine.LSPosedEngine
import me.elhizazi.Castix.privilege.domain.engine.RootEngine
import me.elhizazi.Castix.privilege.domain.engine.ShizukuEngine
import me.elhizazi.Castix.privilege.domain.model.EngineType
import me.elhizazi.Castix.privilege.domain.model.ExecutionResult
import me.elhizazi.Castix.privilege.domain.model.PlaybackOptimizationTask
import me.elhizazi.Castix.privilege.domain.model.ServiceHealth
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.io.File

/**
 * System diagnostic information for Android 17 runtime.
 */
data class SystemDeviceInfo(
    val androidVersion: String = Build.VERSION.RELEASE,
    val apiLevel: Int = Build.VERSION.SDK_INT,
    val deviceModel: String = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}",
    val selinuxMode: String = "Enforcing",
    val architecture: String = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"
)

/**
 * Clean Architecture Repository providing dynamic access to Privilege Escalation strategies.
 */
class PrivilegeEngineRepository(
    val shizukuEngine: ShizukuEngine = ShizukuEngine(),
    val dhizukuEngine: DhizukuEngine = DhizukuEngine(),
    val rootEngine: RootEngine = RootEngine(),
    val lsPosedEngine: LSPosedEngine = LSPosedEngine(),
    val lsPatchEngine: LSPatchEngine = LSPatchEngine(),
    val accessibilityEngine: AccessibilityEngine = AccessibilityEngine()
) {
    private val engines: Map<EngineType, IPrivilegeEngine> = mapOf(
        EngineType.SHIZUKU to shizukuEngine,
        EngineType.DHIZUKU to dhizukuEngine,
        EngineType.ROOT to rootEngine,
        EngineType.LSPOSED to lsPosedEngine,
        EngineType.LSPATCH to lsPatchEngine,
        EngineType.ACCESSIBILITY to accessibilityEngine
    )

    fun getEngine(type: EngineType): IPrivilegeEngine {
        return requireNotNull(engines[type]) { "Unknown privilege engine: $type" }
    }

    fun getAllEngines(): List<IPrivilegeEngine> = engines.values.toList()

    /**
     * Queries all engines concurrently for their installation and authorization status.
     */
    suspend fun queryAllEngineHealth(context: Context): Map<EngineType, ServiceHealth> = coroutineScope {
        val shizukuHealthDeferred = async { shizukuEngine.getHealth(context) }
        val dhizukuHealthDeferred = async { dhizukuEngine.getHealth(context) }
        val rootHealthDeferred = async { rootEngine.getHealth(context) }
        val lsPosedHealthDeferred = async { lsPosedEngine.getHealth(context) }
        val lsPatchHealthDeferred = async { lsPatchEngine.getHealth(context) }
        val accessibilityHealthDeferred = async { accessibilityEngine.getHealth(context) }

        mapOf(
            EngineType.SHIZUKU to shizukuHealthDeferred.await(),
            EngineType.DHIZUKU to dhizukuHealthDeferred.await(),
            EngineType.ROOT to rootHealthDeferred.await(),
            EngineType.LSPOSED to lsPosedHealthDeferred.await(),
            EngineType.LSPATCH to lsPatchHealthDeferred.await(),
            EngineType.ACCESSIBILITY to accessibilityHealthDeferred.await()
        )
    }

    /**
     * Executes arbitrary command using the specified strategy engine.
     */
    suspend fun executeWithEngine(type: EngineType, command: String): ExecutionResult {
        val engine = getEngine(type)
        if (type != EngineType.ROOT) {
            return engine.executeCommand(command)
        }
        return engine.executeCommand(command)
    }

    /**
     * Executes targeted background playback optimization task.
     */
    suspend fun executePlaybackTask(
        type: EngineType,
        task: PlaybackOptimizationTask,
        targetPackage: String
    ): ExecutionResult {
        val engine = getEngine(type)
        return engine.executePlaybackTask(task, targetPackage)
    }

    /**
     * Toggle LSPosed supplementary hook state (for simulation / development testing).
     */
    fun toggleLSPosedModule(active: Boolean) {
        lsPosedEngine.setSimulatedModuleActive(active)
    }

    /**
     * Reads system SELinux and environment info.
     */
    fun getSystemInfo(): SystemDeviceInfo {
        val selinux = try {
            val file = File("/sys/fs/selinux/enforce")
            if (file.exists() && file.canRead()) {
                val content = file.readText().trim()
                if (content == "1") "Enforcing" else "Permissive"
            } else {
                "Enforcing (Android 17)"
            }
        } catch (_: Exception) {
            "Enforcing"
        }

        return SystemDeviceInfo(
            androidVersion = "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            apiLevel = Build.VERSION.SDK_INT,
            deviceModel = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}",
            selinuxMode = selinux,
            architecture = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"
        )
    }
}
