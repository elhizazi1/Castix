package me.elhizazi.Castix.privilege.domain.engine

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.provider.Settings
import me.elhizazi.Castix.privilege.domain.model.EngineHealthStatus
import me.elhizazi.Castix.privilege.domain.model.EngineType
import me.elhizazi.Castix.privilege.domain.model.ExecutionResult
import me.elhizazi.Castix.privilege.domain.model.PlaybackOptimizationTask
import me.elhizazi.Castix.privilege.domain.model.ServiceHealth
import me.elhizazi.Castix.service.CastixAccessibilityService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Standard Android Accessibility Framework Engine (Zero-Root / Zero-Shizuku):
 * Provides all standard non-rooted users with elevated system process priority,
 * instant foreground window state change detection (for the Floating Button),
 * and immune background persistence directly through Android System Settings.
 */
class AccessibilityEngine : IPrivilegeEngine {

    override val engineType: EngineType = EngineType.ACCESSIBILITY
    override val displayName: String = "إمكانية الوصول (Accessibility)"
    override val description: String = "صلاحية نظام رسمية لا تتطلب روت: تتيح كشف التطبيقات المستهدفة وتأمين استمرار الخدمة في الخلفية."

    override suspend fun isAvailable(context: Context): Boolean = true // Always supported on all Android devices

    override suspend fun checkPermission(context: Context): Boolean = withContext(Dispatchers.IO) {
        CastixAccessibilityService.isAccessibilityEnabled(context)
    }

    override suspend fun requestPermission(context: Context): Result<Boolean> = withContext(Dispatchers.Main) {
        runCatching {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        }
    }

    override suspend fun executeCommand(command: String): ExecutionResult {
        val connected = CastixAccessibilityService.isServiceConnected.value
        val isDetection = command.trim().equals("foreground", ignoreCase = true) ||
            command.trim().equals("detect", ignoreCase = true)
        val current = CastixAccessibilityService.getActiveForegroundPackage()
        return ExecutionResult(
            engineUsed = engineType,
            command = command,
            exitCode = if (connected && isDetection && current != null) 0 else 1,
            stdout = if (connected && isDetection && current != null) "Foreground package: $current" else "",
            stderr = if (!connected) "Accessibility service disconnected" else if (!isDetection) "Accessibility cannot execute privileged shell commands" else "Foreground app unknown",
            isSuccess = connected && isDetection && current != null,
            executionTimeMs = 0L
        )
    }

    override suspend fun executePlaybackTask(
        task: PlaybackOptimizationTask,
        targetPackage: String
    ): ExecutionResult = ExecutionResult(
        engineUsed = engineType,
        command = "accessibility::${task.name} --pkg=$targetPackage",
        exitCode = 1,
        stdout = "",
        stderr = "Accessibility events cannot change battery, app-ops, audio focus or phantom-process policies.",
        isSuccess = false,
        executionTimeMs = 0L
    )

    override suspend fun getHealth(context: Context): ServiceHealth = withContext(Dispatchers.IO) {
        val isEnabled = CastixAccessibilityService.isAccessibilityEnabled(context)
        val isConnected = CastixAccessibilityService.isServiceConnected.value
        val isActuallyUsable = isConnected

        val status = when {
            isConnected -> EngineHealthStatus.ACTIVE
            isEnabled -> EngineHealthStatus.AVAILABLE
            else -> EngineHealthStatus.PERMISSION_REQUIRED
        }

        ServiceHealth(
            type = engineType,
            status = status,
            message = if (isConnected) "خدمة إمكانية الوصول نشطة" else if (isEnabled) "إمكانية الوصول مفعلة" else "بحاجة لتفعيل إمكانية الوصول",
            version = "Android Native API",
            isUsable = isActuallyUsable,
            details = if (isActuallyUsable) {
                "خدمة إمكانية الوصول مفعلة بنجاح. تمنح التطبيق أولوية تشغيل مرتفعة ورصد فوري للتطبيقات بدون روت."
            } else {
                "متاحة لجميع الأجهزة بدون روت. اضغط لتفعيل 'Castix' في إعدادات إمكانية الوصول (Accessibility) بهاتفك."
            }
        )
    }
}
