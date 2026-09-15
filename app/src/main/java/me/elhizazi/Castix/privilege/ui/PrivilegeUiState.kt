package me.elhizazi.Castix.privilege.ui

import me.elhizazi.Castix.privilege.domain.model.ConsoleEntry
import me.elhizazi.Castix.privilege.domain.model.EngineType
import me.elhizazi.Castix.privilege.domain.model.FloatingButtonMode
import me.elhizazi.Castix.privilege.domain.model.PlaybackOptimizationTask
import me.elhizazi.Castix.privilege.domain.model.ServiceHealth
import me.elhizazi.Castix.privilege.domain.model.TargetApp
import me.elhizazi.Castix.privilege.domain.repository.SystemDeviceInfo

/**
 * Modern Jetpack Compose UI State representing the current privilege configuration,
 * selected engine strategy, real-time backend statuses, floating trigger, and lock screen integrity.
 */
data class PrivilegeUiState(
    val selectedEngine: EngineType = EngineType.SHIZUKU,
    val engineHealthMap: Map<EngineType, ServiceHealth> = emptyMap(),
    val isRefreshing: Boolean = false,
    val isExecuting: Boolean = false,
    val activePlaybackTasks: Set<PlaybackOptimizationTask> = emptySet(),
    val commandInput: String = "dumpsys audio | grep -i player",
    val consoleLogs: List<ConsoleEntry> = emptyList(),
    val systemInfo: SystemDeviceInfo = SystemDeviceInfo(),
    val notificationMessage: String? = null,
    val isLSPosedHookActive: Boolean = false,
    val showEngineDetailsModal: EngineType? = null,
    val showShizukuDialog: Boolean = false,
    val showLSPosedDialog: Boolean = false,
    // Background Service & Status Bar Notification State
    val isServiceRunning: Boolean = false,
    // Floating Button & Target Apps State
    val isFloatingButtonEnabled: Boolean = true,
    val floatingButtonMode: FloatingButtonMode = FloatingButtonMode.ONLY_SELECTED_APPS,
    val targetApps: List<TargetApp> = emptyList(),
    val selectedTargetPackages: Set<String> = emptySet(),
    val hasOverlayPermission: Boolean = false,
    val hasUsageStatsPermission: Boolean = false,
    val isAccessibilityServiceEnabled: Boolean = false,
    val showTargetAppsDialog: Boolean = false,
    // Lock Screen Compatibility State
    val isDeviceLocked: Boolean = false,
    val isKeyguardSecure: Boolean = false,
    val lockSecurityDescription: String = "",
    val isLockScreenWakeLockActive: Boolean = false,
    val isBatteryOptimizationIgnored: Boolean = false,
    // Black Screen & App Navigation State
    val isBlackScreenActive: Boolean = false,
    val blackScreenDoubleTap: Boolean = true,
    val blackScreenShowClock: Boolean = true,
    val blackScreenZeroBrightness: Boolean = true,
    val currentTab: Int = 0 // 0: Home/Dashboard, 1: Engines, 2: Settings
)
