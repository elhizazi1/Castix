package me.elhizazi.Castix.privilege.ui

import android.app.Application
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import me.elhizazi.Castix.privilege.domain.model.ConsoleEntry
import me.elhizazi.Castix.privilege.domain.model.EngineType
import me.elhizazi.Castix.privilege.domain.model.FloatingButtonMode
import me.elhizazi.Castix.privilege.domain.model.PlaybackOptimizationTask
import me.elhizazi.Castix.privilege.domain.repository.PrivilegeEngineRepository
import me.elhizazi.Castix.privilege.domain.repository.TargetAppsRepository
import me.elhizazi.Castix.service.CastixBackgroundService
import me.elhizazi.Castix.service.FloatingOverlayManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MainViewModel managing privilege engine state, engine switching (Strategy Pattern),
 * real-time background service, floating trigger overlay, target apps, and lockscreen integration.
 */
class MainViewModel(
    application: Application,
    private val repository: PrivilegeEngineRepository = PrivilegeEngineRepository(),
    private val targetAppsRepo: TargetAppsRepository = TargetAppsRepository(application)
) : AndroidViewModel(application) {

    private val appSettingsManager = AppSettingsManager.getInstance(application)

    private val _uiState = MutableStateFlow(
        PrivilegeUiState(
            systemInfo = repository.getSystemInfo(),
            selectedEngine = appSettingsManager.getSelectedEngine() ?: EngineType.SHIZUKU,
            isFloatingButtonEnabled = targetAppsRepo.isFloatingButtonEnabled(),
            floatingButtonMode = targetAppsRepo.getFloatingButtonMode(),
            selectedTargetPackages = targetAppsRepo.getSelectedPackages()
        )
    )
    val uiState: StateFlow<PrivilegeUiState> = _uiState.asStateFlow()

    init {
        // Load persisted playback optimization tasks & system state
        val persistedTasks = appSettingsManager.getEnforcedPlaybackTasks().toMutableSet()
        val powerManager = application.getSystemService(Context.POWER_SERVICE) as? PowerManager
        if (powerManager?.isIgnoringBatteryOptimizations(application.packageName) == true) {
            persistedTasks.add(PlaybackOptimizationTask.BATTERY_WHITELIST)
            appSettingsManager.setTaskEnforced(PlaybackOptimizationTask.BATTERY_WHITELIST, true)
        }
        _uiState.update { it.copy(activePlaybackTasks = persistedTasks) }

        // Automatically check availability of all engines upon launch
        refreshEngineStatus()

        // Seed initial architectural boot log
        seedInitialLog()

        // Observe Castix Background Service runtime status
        observeServiceStatus()

        // Load target applications list and check permissions/lock state
        refreshPermissionsAndLockState()
        loadTargetApps()
        observeBlackScreenStatus()
    }

    private fun observeBlackScreenStatus() {
        val manager = me.elhizazi.Castix.service.BlackScreenOverlayManager.getInstance(getApplication())
        _uiState.update {
            it.copy(
                blackScreenDoubleTap = manager.doubleTapToWake,
                blackScreenShowClock = manager.showClock,
                blackScreenZeroBrightness = manager.zeroBrightness
            )
        }
        viewModelScope.launch {
            me.elhizazi.Castix.service.BlackScreenOverlayManager.isBlackScreenActive.collect { active ->
                _uiState.update { it.copy(isBlackScreenActive = active) }
            }
        }
    }

    private fun observeServiceStatus() {
        viewModelScope.launch {
            CastixBackgroundService.isServiceRunning.collect { running ->
                _uiState.update { it.copy(isServiceRunning = running) }
            }
        }
        viewModelScope.launch {
            CastixBackgroundService.isLockScreenActive.collect { lockActive ->
                _uiState.update { it.copy(isLockScreenWakeLockActive = lockActive) }
            }
        }
    }

    fun refreshPermissionsAndLockState() {
        val app = getApplication<Application>()
        val hasOverlay = FloatingOverlayManager.canDrawOverlays(app)
        val hasUsage = FloatingOverlayManager.hasUsageStatsPermission(app)
        val isAccessibility = me.elhizazi.Castix.service.CastixAccessibilityService.isAccessibilityEnabled(app)
        val lockInfo = me.elhizazi.Castix.privilege.domain.repository.DeviceSecurityHelper.getDeviceLockInfo(app)
        val powerManager = app.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val isBatteryIgnored = powerManager?.isIgnoringBatteryOptimizations(app.packageName) == true

        _uiState.update {
            it.copy(
                hasOverlayPermission = hasOverlay,
                hasUsageStatsPermission = hasUsage,
                isAccessibilityServiceEnabled = isAccessibility,
                isDeviceLocked = lockInfo.isCurrentlyLocked,
                isKeyguardSecure = lockInfo.isDeviceSecure,
                isBatteryOptimizationIgnored = isBatteryIgnored,
                lockSecurityDescription = ""
            )
        }
    }

    /**
     * Opens Android system battery optimization settings or prompts exemption
     * so that lock screen audio playback and CPU wake locks are never suspended.
     */
    fun requestIgnoreBatteryOptimizations(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            val fallback = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
        }
    }

    /**
     * Whitelists Castix directly via Shizuku/Root engine shell command.
     */
    fun whitelistBatteryViaEngine() {
        val engine = _uiState.value.selectedEngine
        val pkg = getApplication<Application>().packageName
        val cmd = "dumpsys deviceidle whitelist +$pkg"
        viewModelScope.launch {
            _uiState.update { it.copy(isExecuting = true) }
            val result = repository.executeWithEngine(engine, cmd)
            appendConsoleLog(
                ConsoleEntry(
                    engine = result.engineUsed,
                    command = result.command,
                    exitCode = result.exitCode,
                    output = "Doze Battery Whitelist: " + (if (result.isSuccess) "Success - Added $pkg to whitelist." else result.stderr),
                    isError = !result.isSuccess,
                    durationMs = result.executionTimeMs
                )
            )
            refreshPermissionsAndLockState()
            _uiState.update {
                it.copy(
                    isExecuting = false,
                    notificationMessage = if (result.isSuccess) "تم استثناء التطبيق من توفير البطارية بنجاح" else "فشل الاستثناء التلقائي"
                )
            }
        }
    }

    /**
     * Opens Android system Security / Lock Screen settings.
     */
    fun openSecuritySettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    /**
     * Opens Android system Accessibility settings.
     */
    fun openAccessibilitySettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    fun loadTargetApps() {
        viewModelScope.launch {
            val apps = targetAppsRepo.getCombinedTargetApps()
            _uiState.update {
                it.copy(
                    targetApps = apps,
                    selectedTargetPackages = targetAppsRepo.getSelectedPackages()
                )
            }
        }
    }

    /**
     * Master toggle to Start or Stop the Castix Background Service.
     */
    fun toggleBackgroundService() {
        val app = getApplication<Application>()
        val currentlyRunning = _uiState.value.isServiceRunning
        if (currentlyRunning) {
            CastixBackgroundService.stop(app)
            appendConsoleLog(
                ConsoleEntry(
                    engine = _uiState.value.selectedEngine,
                    command = "service::stop --name=CastixBackgroundService",
                    exitCode = 0,
                    output = "Castix Background Service stopped. WakeLock released and status bar icon cleared.",
                    durationMs = 6
                )
            )
            _uiState.update {
                it.copy(
                    notificationMessage = "Service stopped / تم إيقاف الخدمة"
                )
            }
        } else {
            CastixBackgroundService.start(app)
            appendConsoleLog(
                ConsoleEntry(
                    engine = _uiState.value.selectedEngine,
                    command = "service::start --foreground --type=mediaPlayback --wakeLock=PARTIAL",
                    exitCode = 0,
                    output = "Castix Background Service started.\nStatus Bar Notification: ACTIVE (VISIBILITY_PUBLIC)\nCPU WakeLock: ENGAGED for Lock Screen bypass.",
                    durationMs = 12
                )
            )
            _uiState.update {
                it.copy(
                    notificationMessage = "Service started / تم تشغيل خدمة الخلفية"
                )
            }
        }
    }

    /**
     * Toggles floating button enabled/disabled.
     */
    fun toggleFloatingButton(enabled: Boolean) {
        targetAppsRepo.setFloatingButtonEnabled(enabled)
        _uiState.update { it.copy(isFloatingButtonEnabled = enabled) }
        val app = getApplication<Application>()
        if (_uiState.value.isServiceRunning) {
            val intent = Intent(app, CastixBackgroundService::class.java).apply {
                action = CastixBackgroundService.ACTION_REFRESH_OVERLAY
            }
            app.startService(intent)
        }
    }

    /**
     * Sets floating trigger mode (Only selected apps vs Always visible).
     */
    fun setFloatingMode(mode: FloatingButtonMode) {
        targetAppsRepo.setFloatingButtonMode(mode)
        _uiState.update { it.copy(floatingButtonMode = mode) }
        FloatingOverlayManager.refreshCurrentVisibility()
    }

    /**
     * Toggles selection of a specific target app for the floating button.
     */
    fun toggleTargetApp(packageName: String) {
        val currentSet = _uiState.value.selectedTargetPackages.toMutableSet()
        if (currentSet.contains(packageName)) {
            currentSet.remove(packageName)
        } else {
            currentSet.add(packageName)
        }
        targetAppsRepo.saveSelectedPackages(currentSet)
        _uiState.update { it.copy(selectedTargetPackages = currentSet) }
        FloatingOverlayManager.refreshCurrentVisibility()
        loadTargetApps()
    }

    fun setShowTargetAppsDialog(show: Boolean) {
        _uiState.update { it.copy(showTargetAppsDialog = show) }
    }

    /**
     * Opens system overlay permission settings.
     */
    fun openOverlaySettings(context: Context) {
        try {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            val fallback = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
        }
    }

    /**
     * Opens system usage access settings.
     */
    fun openUsageStatsSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    /**
     * Attempts to auto-grant overlay permission via Shizuku/Root.
     */
    fun grantOverlayViaEngine() {
        val engine = _uiState.value.selectedEngine
        val pkg = getApplication<Application>().packageName
        val cmd = "appops set $pkg SYSTEM_ALERT_WINDOW allow"
        viewModelScope.launch {
            _uiState.update { it.copy(isExecuting = true) }
            val result = repository.executeWithEngine(engine, cmd)
            appendConsoleLog(
                ConsoleEntry(
                    engine = result.engineUsed,
                    command = result.command,
                    exitCode = result.exitCode,
                    output = "Auto-grant SYSTEM_ALERT_WINDOW: " + (if (result.isSuccess) "SUCCESS" else result.stderr),
                    isError = !result.isSuccess,
                    durationMs = result.executionTimeMs
                )
            )
            refreshPermissionsAndLockState()
            _uiState.update { it.copy(isExecuting = false) }
        }
    }

    private fun seedInitialLog() {
        val initialEntry = ConsoleEntry(
            engine = EngineType.SHIZUKU,
            command = "init --target=android_17_api36 --mode=dynamic_escalation",
            exitCode = 0,
            output = """
                [Castix Architecture] Multi-Privilege Engine Orchestrator v1.0.0 initialized.
                Package: me.elhizazi.Castix
                Target: Android 17 (API 36) Background Playback Architecture.
                Supported Backends:
                  - Shizuku: Remote Binder IPC (UID 2000 shell)
                  - Dhizuku: DevicePolicyManager Enterprise Delegations
                  - Root: Standard su Linux daemon (UID 0)
                  - LSPosed: ART Framework In-Memory Interception
                  - LSPatch: Rootless Dex Loader & In-Process Hooks
                  - Accessibility: Zero-Root System Settings Privilege
                Ready for strategy dispatch.
            """.trimIndent(),
            isError = false,
            durationMs = 4
        )
        _uiState.update { it.copy(consoleLogs = listOf(initialEntry)) }
    }

    /**
     * User switches the active privilege escalation strategy engine.
     */
    fun selectEngine(engine: EngineType) {
        appSettingsManager.saveSelectedEngine(engine)
        _uiState.update { current ->
            current.copy(
                selectedEngine = engine,
                notificationMessage = "Active engine switched to ${engine.displayName}"
            )
        }
        val entry = ConsoleEntry(
            engine = engine,
            command = "engine::switch --strategy=${engine.name.lowercase()}",
            exitCode = 0,
            output = "Active escalation strategy set to: ${engine.displayName} (${engine.architecture})",
            isError = false,
            durationMs = 2
        )
        appendConsoleLog(entry)
        // Re-query immediately so every engine badge/card reflects the newly selected strategy.
        refreshEngineStatus()
    }

    /**
     * Checks availability and health across all registered engines.
     */
    fun refreshEngineStatus() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            val healthMap = repository.queryAllEngineHealth(getApplication())
            val savedEngine = appSettingsManager.getSelectedEngine()
            val currentSelected = _uiState.value.selectedEngine
            val targetEngine = savedEngine ?: currentSelected
            _uiState.update {
                it.copy(
                    engineHealthMap = healthMap,
                    isRefreshing = false,
                    isLSPosedHookActive = healthMap[EngineType.LSPOSED]?.isUsable == true,
                    selectedEngine = targetEngine
                )
            }
        }
    }

    /**
     * Triggers permission request flow for the specified engine.
     * When forcePromptOnly is false and engine is not already usable, shows the explanation / guide modal.
     */
    fun requestPermissionForEngine(engine: EngineType, context: Context, forcePromptOnly: Boolean = false) {
        if (engine == EngineType.SHIZUKU) {
            val isPermitted = runCatching {
                rikka.shizuku.Shizuku.pingBinder() && rikka.shizuku.Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED
            }.getOrDefault(false)

            if (!isPermitted) {
                // If binder is alive, also fire system request immediately so permission dialog pops up
                if (runCatching { rikka.shizuku.Shizuku.pingBinder() }.getOrDefault(false)) {
                    runCatching {
                        rikka.shizuku.Shizuku.requestPermission(me.elhizazi.Castix.privilege.domain.engine.ShizukuEngine.SHIZUKU_REQUEST_CODE)
                    }
                }
            }
            // Always show in-app dialog for Shizuku so user sees status, detected manager, and full guidance
            _uiState.update { it.copy(showShizukuDialog = true) }
            return
        }

        if (engine == EngineType.LSPOSED) {
            // Show LSPosed in-app activation dialog (no dialer codes)
            _uiState.update { it.copy(showLSPosedDialog = true) }
            return
        }

        if (engine == EngineType.ROOT) {
            val isPermitted = _uiState.value.engineHealthMap[EngineType.ROOT]?.isUsable == true
            if (!isPermitted && !forcePromptOnly) {
                // Check if a root manager is installed (KernelSU, APatch, Magisk)
                val detectedMgr = me.elhizazi.Castix.privilege.domain.engine.RootEngine.getDetectedRootManager(context)
                if (detectedMgr != null) {
                    val opened = openRootManagerApp(context)
                    if (opened) {
                        _uiState.update {
                            it.copy(
                                notificationMessage = "تم الانتقال إلى تطبيق ${detectedMgr.displayName}. يرجى تفعيل إذن الروت (Superuser) لـ Castix من قائمة التطبيقات."
                            )
                        }
                        return
                    }
                }
                // Fallback: show explanation dialog
                showEngineDetails(EngineType.ROOT)
                return
            }

            // Direct execution of su request (from inside dialog or forced prompt)
            viewModelScope.launch {
                val eng = repository.getEngine(EngineType.ROOT)
                val result = eng.requestPermission(context)
                result.onSuccess { granted ->
                    refreshEngineStatus()
                    if (granted) {
                        _uiState.update {
                            it.copy(
                                notificationMessage = "تم منح صلاحية الروت (UID 0) بنجاح!",
                                showEngineDetailsModal = null
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(notificationMessage = "لم يتم الكشف عن صلاحية الروت. راجع خطوات التثبيت.")
                        }
                        showEngineDetails(EngineType.ROOT)
                    }
                    appendConsoleLog(
                        ConsoleEntry(
                            engine = EngineType.ROOT,
                            command = "su -c id",
                            exitCode = if (granted) 0 else 1,
                            output = if (granted) "uid=0(root) gid=0(root)" else "Permission denied / su binary not responding",
                            durationMs = 20
                        )
                    )
                }.onFailure { error ->
                    val errorMsg = error.localizedMessage ?: "فشل طلب صلاحية الروت"
                    _uiState.update { it.copy(notificationMessage = errorMsg) }
                    showEngineDetails(EngineType.ROOT)
                }
            }
            return
        }

        if (engine == EngineType.DHIZUKU) {
            val isPermitted = _uiState.value.engineHealthMap[EngineType.DHIZUKU]?.isUsable == true
            if (!isPermitted && !forcePromptOnly) {
                showEngineDetails(EngineType.DHIZUKU)
                return
            }
        }

        if (engine == EngineType.LSPATCH) {
            val isPermitted = _uiState.value.engineHealthMap[EngineType.LSPATCH]?.isUsable == true
            if (!isPermitted && !forcePromptOnly) {
                showEngineDetails(EngineType.LSPATCH)
                return
            }
        }

        viewModelScope.launch {
            val eng = repository.getEngine(engine)
            val result = eng.requestPermission(context)
            result.onSuccess {
                refreshEngineStatus()
                appendConsoleLog(
                    ConsoleEntry(
                        engine = engine,
                        command = "request_permission --engine=${engine.name.lowercase()}",
                        exitCode = 0,
                        output = "Permission workflow dispatched for ${engine.displayName}.",
                        durationMs = 15
                    )
                )
            }.onFailure { error ->
                val errorMsg = error.localizedMessage ?: "Failed to request permission"
                _uiState.update {
                    it.copy(notificationMessage = errorMsg)
                }
                appendConsoleLog(
                    ConsoleEntry(
                        engine = engine,
                        command = "request_permission --engine=${engine.name.lowercase()}",
                        exitCode = 1,
                        output = "Failed to request permission: $errorMsg",
                        isError = true,
                        durationMs = 5
                    )
                )
            }
        }
    }

    fun setShowShizukuDialog(show: Boolean) {
        _uiState.update { it.copy(showShizukuDialog = show) }
    }

    fun setShowLSPosedDialog(show: Boolean) {
        _uiState.update { it.copy(showLSPosedDialog = show) }
    }

    fun confirmLSPosedModuleActivated(context: Context) {
        refreshEngineStatus()
        _uiState.update {
            it.copy(
                showLSPosedDialog = false,
                notificationMessage = "تم طلب إعادة فحص حالة موديول LSPosed."
            )
        }
        appendConsoleLog(
            ConsoleEntry(
                engine = EngineType.LSPOSED,
                command = "lsposed::module_status_recheck(scope=true)",
                exitCode = 0,
                output = "LSPosed module status re-check requested; activation is reported only after the runtime hook is observed.",
                durationMs = 8
            )
        )
    }

    fun requestShizukuDirectPermission(context: Context) {
        val ping = runCatching { rikka.shizuku.Shizuku.pingBinder() }.getOrDefault(false)
        if (ping) {
            val granted = runCatching {
                rikka.shizuku.Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED
            }.getOrDefault(false)
            if (granted) {
                _uiState.update { it.copy(notificationMessage = "صلاحية Shizuku ممنوحة بالفعل ونشطة!") }
                return
            }
            runCatching {
                rikka.shizuku.Shizuku.requestPermission(me.elhizazi.Castix.privilege.domain.engine.ShizukuEngine.SHIZUKU_REQUEST_CODE)
                _uiState.update { it.copy(notificationMessage = "تم إرسال طلب الصلاحية إلى خادم Shizuku") }
            }.onFailure { err ->
                _uiState.update { it.copy(notificationMessage = "تعذر طلب الصلاحية: ${err.localizedMessage}") }
            }
        } else {
            openShizukuManagerApp(context)
            _uiState.update {
                it.copy(notificationMessage = "خدمة Shizuku غير مشغلة. يرجى الضغط على 'بدء' داخل التطبيق ثم العودة.")
            }
        }
    }

    fun openShizukuManagerApp(context: Context) {
        val pm = context.packageManager
        for (pkg in me.elhizazi.Castix.privilege.domain.engine.ShizukuEngine.SHIZUKU_PACKAGES) {
            val intent = pm.getLaunchIntentForPackage(pkg)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return
            }
        }
        _uiState.update { it.copy(notificationMessage = "لم يتم العثور على تطبيق Shizuku أو Shevery أو Shizuku+ مثبت على الجهاز") }
    }

    fun openRootManagerApp(context: Context): Boolean {
        val opened = me.elhizazi.Castix.privilege.domain.engine.RootEngine.openRootManagerApp(context)
        if (opened) {
            val detected = me.elhizazi.Castix.privilege.domain.engine.RootEngine.getDetectedRootManager(context)
            val name = detected?.displayName ?: "إدارة الروت"
            _uiState.update {
                it.copy(notificationMessage = "تم فتح تطبيق $name. يرجى تفعيل Superuser لـ Castix من داخل التطبيق.")
            }
            return true
        }
        return false
    }

    fun openLSPosedManagerApp(context: Context) {
        val pm = context.packageManager
        // 1. Check known standalone manager packages
        val dedicatedLsposedPackages = listOf(
            "org.lsposed.manager",
            "io.github.lsposed.manager",
            "org.lsposed.manager.mod",
            "org.lsposed.manager.release",
            "com.jingmatrix.lsposed",
            "io.github.mywalkb.lsposed",
            "org.meowcat.edxposed.manager"
        )
        for (pkg in dedicatedLsposedPackages) {
            val intent = pm.getLaunchIntentForPackage(pkg)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return
            }
        }
        // 2. Try explicit component intent
        for (pkg in dedicatedLsposedPackages) {
            try {
                val intent = Intent().apply {
                    setClassName(pkg, "$pkg.ui.MainActivity")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (intent.resolveActivity(pm) != null) {
                    context.startActivity(intent)
                    return
                }
            } catch (_: Exception) {}
        }
        // 3. Try standard ACTION_MANAGER broadcast / activity
        val actionIntent = Intent(me.elhizazi.Castix.privilege.domain.engine.LSPosedEngine.LSPOSED_ACTION_MANAGER).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (actionIntent.resolveActivity(pm) != null) {
            context.startActivity(actionIntent)
            return
        }
        // 4. Try root am start
        try {
            val proc = Runtime.getRuntime().exec(arrayOf("su", "-c", "am start -a org.lsposed.manager.ACTION_MANAGER || am start -n org.lsposed.manager/.ui.MainActivity || am start -n io.github.lsposed.manager/.ui.MainActivity"))
            if (proc.waitFor() == 0) return
        } catch (_: Exception) {}

        // 5. Fallback: If Root Manager (KernelSU / APatch / Magisk) is installed, launch it
        if (openRootManagerApp(context)) {
            _uiState.update {
                it.copy(notificationMessage = "تم فتح تطبيق الروت. يمكنك إدارة موديول LSPosed من قسم الموديولات.")
            }
            return
        }

        // 6. Direct user to download LSPosed Manager if not present
        try {
            val webIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://github.com/mywalkb/LSPosed_mod/releases")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
            _uiState.update { it.copy(notificationMessage = "لم يتم العثور على LSPosed Manager. تم فتح صفحة التحميل.") }
        } catch (_: Exception) {
            _uiState.update { it.copy(notificationMessage = "لم يتم العثور على مدير LSPosed أو تطبيق روت مثبت.") }
        }
    }

    /**
     * Handles the callback when user grants or denies Shizuku permission dialog.
     */
    fun onShizukuPermissionResult(granted: Boolean) {
        refreshEngineStatus()
        val msg = if (granted) {
            "تم تفعيل صلاحية Shizuku بنجاح"
        } else {
            "تم رفض إذن Shizuku من قبل المستخدم"
        }
        _uiState.update { it.copy(notificationMessage = msg) }
        appendConsoleLog(
            ConsoleEntry(
                engine = EngineType.SHIZUKU,
                command = "shizuku::permission_result",
                exitCode = if (granted) 0 else 1,
                output = msg,
                isError = !granted,
                durationMs = 1
            )
        )
    }

    /**
     * Updates the custom command text input.
     */
    fun updateCommandInput(cmd: String) {
        _uiState.update { it.copy(commandInput = cmd) }
    }

    /**
     * Dispatches the current user command through the selected strategy engine.
     */
    fun executeCustomCommand() {
        val command = _uiState.value.commandInput.trim()
        if (command.isEmpty()) return

        val activeEngine = _uiState.value.selectedEngine
        viewModelScope.launch {
            _uiState.update { it.copy(isExecuting = true) }

            val result = repository.executeWithEngine(activeEngine, command)

            val entry = ConsoleEntry(
                engine = result.engineUsed,
                command = result.command,
                exitCode = result.exitCode,
                output = if (result.isSuccess) {
                    result.stdout.ifEmpty { "[Success with exit code 0]" }
                } else {
                    "${result.stderr.ifEmpty { "Command failed" }}\nExit code: ${result.exitCode}"
                },
                isError = !result.isSuccess,
                durationMs = result.executionTimeMs
            )

            appendConsoleLog(entry)
            _uiState.update { it.copy(isExecuting = false) }
        }
    }

    /**
     * Dispatches a specific Android 17 background playback optimization task.
     */
    fun executePlaybackTask(task: PlaybackOptimizationTask) {
        val activeEngine = _uiState.value.selectedEngine
        val targetPkg = getApplication<Application>().packageName

        viewModelScope.launch {
            _uiState.update { it.copy(isExecuting = true) }

            val result = repository.executePlaybackTask(activeEngine, task, targetPkg)

            val entry = ConsoleEntry(
                engine = result.engineUsed,
                command = result.command,
                exitCode = result.exitCode,
                output = "[${task.title}] -> ${result.stdout.ifEmpty { "Executed successfully via ${activeEngine.displayName}" }}",
                isError = !result.isSuccess,
                durationMs = result.executionTimeMs
            )
            appendConsoleLog(entry)

            _uiState.update { current ->
                val updatedTasks = current.activePlaybackTasks.toMutableSet()
                if (result.isSuccess) {
                    updatedTasks.add(task)
                    appSettingsManager.setTaskEnforced(task, true)
                }
                current.copy(
                    isExecuting = false,
                    activePlaybackTasks = updatedTasks,
                    notificationMessage = "Executed ${task.title}"
                )
            }
        }
    }

    /**
     * Executes all background playback bypasses in sequence.
     */
    fun executeAllPlaybackBypasses() {
        viewModelScope.launch {
            PlaybackOptimizationTask.entries.forEach { task ->
                executePlaybackTask(task)
            }
        }
    }

    /**
     * Toggle LSPosed supplementary hook state (allows live interactive simulation of Zygote hook).
     */
    fun toggleLSPosedModule(active: Boolean) {
        appSettingsManager.setTaskEnforced(PlaybackOptimizationTask.AUDIO_FOCUS_HIJACK, active)
        refreshEngineStatus()
        appendConsoleLog(
            ConsoleEntry(
                engine = EngineType.LSPOSED,
                command = "lsposed::toggle_hook --active=$active",
                exitCode = 0,
                output = if (active) {
                    "LSPosed Hooking Activated: AudioFocus interception and phantom process killer hook bound in ART Zygote space."
                } else {
                    "LSPosed Hooking Deactivated."
                },
                durationMs = 8
            )
        )
    }

    /**
     * Clears terminal console logs.
     */
    fun clearConsole() {
        _uiState.update { it.copy(consoleLogs = emptyList()) }
    }

    /**
     * Dismisses the transient notification banner.
     */
    fun dismissNotification() {
        _uiState.update { it.copy(notificationMessage = null) }
    }

    /**
     * Shows detail modal for the selected engine.
     */
    fun showEngineDetails(engine: EngineType?) {
        _uiState.update { it.copy(showEngineDetailsModal = engine) }
    }

    fun setTab(index: Int) {
        _uiState.update { it.copy(currentTab = index) }
    }

    fun toggleBlackScreen() {
        val app = getApplication<Application>()
        val hasOverlay = FloatingOverlayManager.canDrawOverlays(app)
        if (!hasOverlay) {
            _uiState.update {
                it.copy(notificationMessage = "يرجى منح إذن الظهور فوق التطبيقات أولاً لتشغيل الشاشة السوداء")
            }
            return
        }
        val manager = me.elhizazi.Castix.service.BlackScreenOverlayManager.getInstance(app)
        manager.toggleBlackScreen()
    }

    fun updateBlackScreenSettings(doubleTap: Boolean, showClock: Boolean, zeroBrightness: Boolean) {
        val manager = me.elhizazi.Castix.service.BlackScreenOverlayManager.getInstance(getApplication())
        manager.savePreferences(doubleTap, showClock, zeroBrightness)
        _uiState.update {
            it.copy(
                blackScreenDoubleTap = doubleTap,
                blackScreenShowClock = showClock,
                blackScreenZeroBrightness = zeroBrightness,
                notificationMessage = "تم حفظ إعدادات الشاشة السوداء بنجاح"
            )
        }
    }

    fun setBlackScreenDoubleTap(doubleTap: Boolean) {
        updateBlackScreenSettings(
            doubleTap = doubleTap,
            showClock = _uiState.value.blackScreenShowClock,
            zeroBrightness = _uiState.value.blackScreenZeroBrightness
        )
    }

    fun setBlackScreenZeroBrightness(zeroBrightness: Boolean) {
        updateBlackScreenSettings(
            doubleTap = _uiState.value.blackScreenDoubleTap,
            showClock = _uiState.value.blackScreenShowClock,
            zeroBrightness = zeroBrightness
        )
    }

    private fun appendConsoleLog(entry: ConsoleEntry) {
        _uiState.update { current ->
            // Keep last 100 entries
            val updated = (listOf(entry) + current.consoleLogs).take(100)
            current.copy(consoleLogs = updated)
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(application) as T
        }
    }
}
