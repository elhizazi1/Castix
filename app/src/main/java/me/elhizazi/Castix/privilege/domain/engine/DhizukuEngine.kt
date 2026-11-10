package me.elhizazi.Castix.privilege.domain.engine

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.rosan.dhizuku.api.Dhizuku
import com.rosan.dhizuku.api.DhizukuRequestPermissionListener
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import me.elhizazi.Castix.privilege.domain.model.EngineHealthStatus
import me.elhizazi.Castix.privilege.domain.model.EngineType
import me.elhizazi.Castix.privilege.domain.model.ExecutionResult
import me.elhizazi.Castix.privilege.domain.model.PlaybackOptimizationTask
import me.elhizazi.Castix.privilege.domain.model.ServiceHealth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Dhizuku Privilege Escalation Engine:
 * Communicates with the Dhizuku Device Owner provider to delegate DevicePolicyManager
 * privileges to non-Device Owner applications without requiring a full superuser daemon.
 */
class DhizukuEngine : IPrivilegeEngine {

    override val engineType: EngineType = EngineType.DHIZUKU
    override val displayName: String = "Dhizuku (Device Owner)"
    override val description: String = "Device Owner delegation API for system-level policy & background exemptions."

    companion object {
        const val DHIZUKU_PACKAGE = "com.rosan.dhizuku"
        const val DHIZUKU_PERMISSION = "com.rosan.dhizuku.permission.API"
        const val DHIZUKU_AUTHORITY = "com.rosan.dhizuku.provider"
        const val ACTION_REQUEST_PERMISSION = "com.rosan.dhizuku.action.REQUEST_PERMISSION"
    }

    override suspend fun isAvailable(context: Context): Boolean = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val isPackageInstalled = runCatching {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(DHIZUKU_PACKAGE, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(DHIZUKU_PACKAGE, 0)
            }
            true
        }.getOrDefault(false)

        val isProviderResolved = runCatching {
            pm.resolveContentProvider(DHIZUKU_AUTHORITY, 0) != null
        }.getOrDefault(false)

        isPackageInstalled || isProviderResolved
    }

    private fun initialize(context: Context): Boolean = runCatching {
        Dhizuku.init(context.applicationContext)
    }.getOrDefault(false)

    override suspend fun checkPermission(context: Context): Boolean = withContext(Dispatchers.IO) {
        if (!initialize(context)) return@withContext false
        runCatching { Dhizuku.isPermissionGranted() }.getOrDefault(false)
    }

    override suspend fun requestPermission(context: Context): Result<Boolean> = withContext(Dispatchers.Main) {
        if (!initialize(context)) {
            return@withContext Result.failure(IllegalStateException("Dhizuku service unavailable. Start Dhizuku first."))
        }
        if (runCatching { Dhizuku.isPermissionGranted() }.getOrDefault(false)) {
            return@withContext Result.success(true)
        }
        try {
            val granted = withTimeoutOrNull(60000L) {
                suspendCancellableCoroutine<Boolean> { continuation ->
                    try {
                        Dhizuku.requestPermission(object : DhizukuRequestPermissionListener() {
                            override fun onRequestPermission(grantResult: Int) {
                                if (continuation.isActive) continuation.resume(grantResult == PackageManager.PERMISSION_GRANTED)
                            }
                        })
                    } catch (error: Throwable) {
                        if (continuation.isActive) continuation.cancel(error)
                    }
                }
            } ?: false
            Result.success(granted)
        } catch (error: Throwable) {
            Result.failure(error)
        }
    }

    // Dhizuku's DevicePolicyManager delegation is not a privileged shell.
    // Running `sh -c` here used Castix's ordinary app UID and falsely implied
    // Device Owner authority. Fail closed until real DPM binder calls exist.
    private fun unsupported(command: String) = ExecutionResult(
        command = command,
        exitCode = 1,
        stdout = "",
        stderr = "Dhizuku does not provide a privileged shell. This operation requires an implemented, authorized DevicePolicyManager API call.",
        executionTimeMs = 0L,
        engineUsed = engineType,
        isSuccess = false
    )

    override suspend fun executeCommand(command: String): ExecutionResult = unsupported(command)

    override suspend fun executePlaybackTask(
        task: PlaybackOptimizationTask,
        targetPackage: String
    ): ExecutionResult = unsupported("dhizuku::${task.name} --pkg=$targetPackage")

    override suspend fun getHealth(context: Context): ServiceHealth = withContext(Dispatchers.IO) {
        val available = isAvailable(context)
        val permitted = checkPermission(context)

        when {
            available && permitted -> {
                ServiceHealth(
                    type = EngineType.DHIZUKU,
                    status = EngineHealthStatus.ACTIVE,
                    message = "Dhizuku API permission granted; policy tasks not implemented",
                    version = "v2.8.2 (DO Component)",
                    isUsable = false,
                    details = "Dhizuku authorization detected, but Castix has no implemented DevicePolicyManager dispatch."
                )
            }
            available && !permitted -> {
                ServiceHealth(
                    type = EngineType.DHIZUKU,
                    status = EngineHealthStatus.PERMISSION_REQUIRED,
                    message = "Dhizuku installed; delegation pending",
                    version = "v2.8.2",
                    isUsable = false,
                    details = "Grant Device Owner delegation in Dhizuku Manager."
                )
            }
            else -> {
                ServiceHealth(
                    type = EngineType.DHIZUKU,
                    status = EngineHealthStatus.NOT_INSTALLED,
                    message = "Dhizuku DO Provider not detected",
                    version = null,
                    isUsable = false,
                    details = "Requires device owner setup via adb dpm or QR provisioning."
                )
            }
        }
    }
}
