package me.elhizazi.Castix.privilege.domain.engine

import android.content.Context
import me.elhizazi.Castix.privilege.domain.model.EngineType
import me.elhizazi.Castix.privilege.domain.model.ExecutionResult
import me.elhizazi.Castix.privilege.domain.model.PlaybackOptimizationTask
import me.elhizazi.Castix.privilege.domain.model.ServiceHealth

/**
 * Strategy Pattern Interface for Android Privilege Escalation Engines.
 * Encapsulates the communication protocol (Binder IPC, DevicePolicyManager, su Shell, or Xposed Hook).
 */
interface IPrivilegeEngine {
    val engineType: EngineType
    val displayName: String
    val description: String

    /**
     * Checks whether the underlying provider, manager service, or binary exists on the system.
     */
    suspend fun isAvailable(context: Context): Boolean

    /**
     * Checks whether authorization has been granted to invoke privileged operations.
     */
    suspend fun checkPermission(context: Context): Boolean

    /**
     * Triggers permission grant or authentication request flow for this engine.
     */
    suspend fun requestPermission(context: Context): Result<Boolean>

    /**
     * Dispatches an arbitrary privileged command string and captures exit status and stream outputs.
     */
    suspend fun executeCommand(command: String): ExecutionResult

    /**
     * Dispatches optimized Android 17 background playback maintenance tasks.
     */
    suspend fun executePlaybackTask(
        task: PlaybackOptimizationTask,
        targetPackage: String
    ): ExecutionResult

    /**
     * Diagnoses runtime health, installed version, and connection state.
     */
    suspend fun getHealth(context: Context): ServiceHealth
}
