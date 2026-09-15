package me.elhizazi.Castix.privilege.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.elhizazi.Castix.R
import me.elhizazi.Castix.privilege.domain.model.EngineType
import me.elhizazi.Castix.privilege.ui.components.BlackScreenQuickCard
import me.elhizazi.Castix.privilege.ui.components.EngineDetailsDialog
import me.elhizazi.Castix.privilege.ui.components.SimpleEngineSelectorCard
import me.elhizazi.Castix.privilege.ui.components.FloatingTriggerCard
import me.elhizazi.Castix.privilege.ui.components.LockScreenCard
import me.elhizazi.Castix.privilege.ui.components.PlaybackBypassesDeck
import me.elhizazi.Castix.privilege.ui.components.PrivilegedTerminalCard
import me.elhizazi.Castix.privilege.ui.components.ServiceControlCard
import me.elhizazi.Castix.privilege.ui.components.ServiceStatusRow
import me.elhizazi.Castix.privilege.ui.components.TargetAppsDialog
import me.elhizazi.Castix.privilege.ui.components.ShizukuPermissionDialog
import me.elhizazi.Castix.privilege.ui.components.LSPosedActivationDialog
import me.elhizazi.Castix.privilege.ui.components.CustomBottomNavigationBar
import me.elhizazi.Castix.privilege.ui.components.HomeHeader
import me.elhizazi.Castix.ui.theme.NeonCyan
import me.elhizazi.Castix.ui.theme.NeonGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivilegeHomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val appSettings by AppSettingsManager.getInstance(context).settings.collectAsState()
    val lang = remember(appSettings.language) { AppSettingsManager.getEffectiveLanguage(appSettings.language) }
    val currentAccent = MaterialTheme.colorScheme.primary

    var showHowToUseBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            val engineIcon = when (uiState.selectedEngine) {
                EngineType.SHIZUKU -> Icons.Default.Security
                EngineType.ROOT -> Icons.Default.Terminal
                EngineType.ACCESSIBILITY -> Icons.Default.AccessibilityNew
                EngineType.LSPOSED -> Icons.Default.Extension
                EngineType.LSPATCH -> Icons.Default.Layers
                EngineType.DHIZUKU -> Icons.Default.Shield
            }

            val selectedHealth = uiState.engineHealthMap[uiState.selectedEngine]
            val isEngineUsable = selectedHealth?.isUsable == true

            HomeHeader(
                isEngineUsable = isEngineUsable,
                engineIcon = engineIcon,
                engineName = uiState.selectedEngine.getLocalizedName(lang),
                onInfoClick = { showHowToUseBottomSheet = true },
                onEngineClick = { viewModel.showEngineDetails(uiState.selectedEngine) },
                lang = lang
            )
        },
        bottomBar = {
            CustomBottomNavigationBar(
                selectedTab = uiState.currentTab,
                onTabSelected = { viewModel.setTab(it) },
                lang = lang
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

        AnimatedContent(
            targetState = uiState.currentTab,
            transitionSpec = {
                val movingForward = targetState > initialState
                val sign = if (isRtl) (if (movingForward) -1 else 1) else (if (movingForward) 1 else -1)

                val slideIn = slideInHorizontally(
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                    initialOffsetX = { fullWidth -> (sign * (fullWidth * 0.18f)).toInt() }
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
                )

                val slideOut = slideOutHorizontally(
                    animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing),
                    targetOffsetX = { fullWidth -> (-sign * (fullWidth * 0.18f)).toInt() }
                ) + fadeOut(
                    animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing)
                )

                slideIn.togetherWith(slideOut)
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            label = "Tab_Synchronized_Transition"
        ) { currentTab ->
            when (currentTab) {
                    3 -> {
                        // Tab 3: Settings Screen (Dedicated Consumer Settings)
                        SettingsScreen(
                            uiState = uiState,
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    1 -> {
                        // Tab 1: Dedicated AMOLED Customization Screen
                        AmoledScreen(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    2 -> {
                        // Tab 2: System Privilege Engines & Tools
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item(key = "engine_selector_card") {
                                SimpleEngineSelectorCard(
                                    selectedEngine = uiState.selectedEngine,
                                    healthMap = uiState.engineHealthMap,
                                    onSelectEngine = { engine ->
                                        viewModel.selectEngine(engine)
                                    },
                                    onRequestPermission = { engine ->
                                        viewModel.requestPermissionForEngine(engine, context)
                                    },
                                    onShowEngineDetails = { engine ->
                                        viewModel.showEngineDetails(engine)
                                    },
                                    lang = lang
                                )
                            }

                            item(key = "playback_utility_deck") {
                                PlaybackBypassesDeck(
                                    activeEngine = uiState.selectedEngine,
                                    activeTasks = uiState.activePlaybackTasks,
                                    isExecuting = uiState.isExecuting,
                                    isLSPosedHookActive = uiState.isLSPosedHookActive,
                                    onExecuteTask = { task ->
                                        viewModel.executePlaybackTask(task)
                                    },
                                    onExecuteAllTasks = {
                                        viewModel.executeAllPlaybackBypasses()
                                    },
                                    onToggleLSPosedHook = { active ->
                                        viewModel.toggleLSPosedModule(active)
                                    }
                                )
                            }

                            item(key = "terminal_console_card") {
                                PrivilegedTerminalCard(
                                    activeEngine = uiState.selectedEngine,
                                    commandInput = uiState.commandInput,
                                    logs = uiState.consoleLogs,
                                    isExecuting = uiState.isExecuting,
                                    onCommandChange = { cmd ->
                                        viewModel.updateCommandInput(cmd)
                                    },
                                    onExecuteCommand = {
                                        viewModel.executeCustomCommand()
                                    },
                                    onClearLogs = {
                                        viewModel.clearConsole()
                                    }
                                )
                            }

                            item(key = "system_info_footer") {
                                SystemInfoCard(systemInfo = uiState.systemInfo)
                            }
                        }
                    }
                    else -> {
                        // Tab 0: Home Dashboard (Clean, Focused on Black Screen & Background Playback)
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Transient Notification / Feedback Banner
                            item(key = "notification_banner") {
                                AnimatedVisibility(
                                    visible = uiState.notificationMessage != null,
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    uiState.notificationMessage?.let { msg ->
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    modifier = Modifier.weight(1f),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Info,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Text(
                                                        text = msg,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                                    )
                                                }

                                                IconButton(
                                                    onClick = { viewModel.dismissNotification() },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = AppStrings.text("btn_dismiss"),
                                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 1. Black Screen Quick Action Card (Immediate Screen Off / AMOLED Black)
                            item(key = "black_screen_card") {
                                BlackScreenQuickCard(
                                    isBlackScreenActive = uiState.isBlackScreenActive,
                                    hasOverlayPermission = uiState.hasOverlayPermission,
                                    isServiceRunning = uiState.isServiceRunning,
                                    isFloatingButtonEnabled = uiState.isFloatingButtonEnabled,
                                    onToggleBlackScreen = { viewModel.toggleBlackScreen() },
                                    onRequestOverlayPermission = { viewModel.openOverlaySettings(context) }
                                )
                            }

                            // 2. Master Foreground Service Control (Start / Stop & Status Bar Icon)
                            item(key = "service_control_card") {
                                ServiceControlCard(
                                    isServiceRunning = uiState.isServiceRunning,
                                    isLockScreenWakeLockActive = uiState.isLockScreenWakeLockActive,
                                    onToggleService = { viewModel.toggleBackgroundService() }
                                )
                            }

                            // 3. Floating Trigger & Target Applications Whitelist Card
                            item(key = "floating_trigger_card") {
                                FloatingTriggerCard(
                                    isEnabled = uiState.isFloatingButtonEnabled,
                                    mode = uiState.floatingButtonMode,
                                    selectedAppsCount = uiState.selectedTargetPackages.size,
                                    hasOverlayPermission = uiState.hasOverlayPermission,
                                    isAccessibilityEnabled = uiState.isAccessibilityServiceEnabled,
                                    onToggleEnabled = { viewModel.toggleFloatingButton(it) },
                                    onSetMode = { viewModel.setFloatingMode(it) },
                                    onOpenAppsDialog = { viewModel.setShowTargetAppsDialog(true) },
                                    onRequestOverlayPermission = { viewModel.openOverlaySettings(context) },
                                    onAutoGrantViaEngine = { viewModel.grantOverlayViaEngine() },
                                    onRequestAccessibility = { viewModel.openAccessibilitySettings(context) }
                                )
                            }

                            // 4. Device Lock & Lock Screen Compatibility Card
                            item(key = "lock_screen_card") {
                                LockScreenCard(
                                    isDeviceLocked = uiState.isDeviceLocked,
                                    isKeyguardSecure = uiState.isKeyguardSecure,
                                    lockSecurityDescription = uiState.lockSecurityDescription,
                                    isServiceRunning = uiState.isServiceRunning,
                                    isBatteryOptimizationIgnored = uiState.isBatteryOptimizationIgnored,
                                    onOpenSecuritySettings = { viewModel.openSecuritySettings(context) },
                                    onRequestDisableBatteryOptimization = { viewModel.requestIgnoreBatteryOptimizations(context) },
                                    onAutoGrantBattery = { viewModel.whitelistBatteryViaEngine() }
                                )
                            }
                        }
                    }
                }
            }
        }

    // Modal Bottom Sheet for Engine Details
    uiState.showEngineDetailsModal?.let { modalEngine ->
        EngineDetailsDialog(
            engine = modalEngine,
            health = uiState.engineHealthMap[modalEngine],
            onDismiss = { viewModel.showEngineDetails(null) },
            onRequestPermission = {
                viewModel.requestPermissionForEngine(modalEngine, context, forcePromptOnly = true)
            }
        )
    }

    // Modal Bottom Sheet for Whitelisted Target Applications
    if (uiState.showTargetAppsDialog) {
        TargetAppsDialog(
            targetApps = uiState.targetApps,
            selectedPackages = uiState.selectedTargetPackages,
            onToggleApp = { viewModel.toggleTargetApp(it) },
            onDismiss = { viewModel.setShowTargetAppsDialog(false) }
        )
    }

    // Modal Bottom Sheet for Shizuku / Shevery / Shizuku+ Permission Workflow
    if (uiState.showShizukuDialog) {
        ShizukuPermissionDialog(
            isPermitted = uiState.engineHealthMap[EngineType.SHIZUKU]?.isUsable == true,
            onDismiss = { viewModel.setShowShizukuDialog(false) },
            onRequestDirectPermission = { viewModel.requestShizukuDirectPermission(context) },
            onOpenManagerApp = { viewModel.openShizukuManagerApp(context) },
            onRefreshStatus = { viewModel.refreshEngineStatus() }
        )
    }

    // Modal Bottom Sheet for LSPosed / Xposed Module Activation Workflow
    if (uiState.showLSPosedDialog) {
        LSPosedActivationDialog(
            isModuleActive = uiState.isLSPosedHookActive,
            onDismiss = { viewModel.setShowLSPosedDialog(false) },
            onRefreshStatus = { viewModel.refreshEngineStatus() },
            onConfirmActivated = { viewModel.confirmLSPosedModuleActivated(context) }
        )
    }

    if (showHowToUseBottomSheet) {
        me.elhizazi.Castix.privilege.ui.components.HowToUseBottomSheet(
            onDismiss = { showHowToUseBottomSheet = false }
        )
    }
}

@Composable
fun SystemInfoCard(
    systemInfo: me.elhizazi.Castix.privilege.domain.repository.SystemDeviceInfo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("system_info_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = AppStrings.text("runtime_environment"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SystemMetricItem(label = AppStrings.text("target_platform"), value = systemInfo.androidVersion)
                        SystemMetricItem(
                            label = AppStrings.text("api_level"),
                            value = "${systemInfo.apiLevel} (Baklava)"
                        )
                        SystemMetricItem(label = AppStrings.text("selinux_mode"), value = systemInfo.selinuxMode)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SystemMetricItem(label = AppStrings.text("device_hardware"), value = systemInfo.deviceModel)
                        SystemMetricItem(label = AppStrings.text("architecture"), value = systemInfo.architecture)
                    }
                }
            }
        }
    }
}

@Composable
fun SystemMetricItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
