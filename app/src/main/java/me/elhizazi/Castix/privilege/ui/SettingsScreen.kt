package me.elhizazi.Castix.privilege.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.elhizazi.Castix.privilege.domain.model.EngineHealthStatus
import me.elhizazi.Castix.privilege.ui.components.HexColorBottomSheet
import me.elhizazi.Castix.privilege.ui.components.HowToUseBottomSheet
import me.elhizazi.Castix.privilege.ui.components.LanguagePickerBottomSheet
import me.elhizazi.Castix.privilege.ui.components.SupportBottomSheet
import me.elhizazi.Castix.ui.theme.AmethystPurple
import me.elhizazi.Castix.ui.theme.CrimsonRed
import me.elhizazi.Castix.ui.theme.EmeraldGreen
import me.elhizazi.Castix.ui.theme.NeonGreen
import me.elhizazi.Castix.ui.theme.SapphireBlue
import me.elhizazi.Castix.ui.theme.SiwaneTeal
import me.elhizazi.Castix.ui.theme.SunsetAmber

private data class ColorPresetItem(
    val nameKey: String,
    val defaultName: String,
    val hex: String,
    val color: Color
)

/**
 * Modern, refined Settings Screen redesigned precisely to match the user's
 * elegant, high-craft layout:
 * 1. Language selector pill card with globe icon and chevron
 * 2. "المظهر والتصميم" card with:
 *    - "وضع العرض" pill segmented selector (داكن, فاتح, تلقائي مع علامة الصح)
 *    - "لوحة الألوان" with 6 color chips (ياقوتي, زمردي, جمشت, غروب, قرمزي, Siwane)
 *    - Two wide action buttons (Dynamic M3, Hex مخصص)
 *    - "مقياس الواجهة والخطوط" with step slider and percentage indicator
 * 3. Engine & System Privilege card (matching account/core auth design)
 * 4. Background service and AMOLED black screen controls
 * 5. Floating button customization & About
 */
@Composable
fun SettingsScreen(
    uiState: PrivilegeUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settingsManager = remember { AppSettingsManager.getInstance(context) }
    val appSettings by settingsManager.settings.collectAsState()
    val lang = appSettings.language
    val currentAppLanguage = remember(lang) { LanguageRegistry.findLanguage(lang) }
    val selectedEngineHealth = uiState.engineHealthMap[uiState.selectedEngine]
    val coreStatus = selectedEngineHealth?.status ?: EngineHealthStatus.NOT_INSTALLED
    val coreStatusLabel = when (coreStatus) {
        EngineHealthStatus.ACTIVE -> AppStrings.get("status_active", lang)
        EngineHealthStatus.AVAILABLE -> AppStrings.get("status_available", lang)
        EngineHealthStatus.PERMISSION_REQUIRED -> AppStrings.get("status_perm_needed", lang)
        EngineHealthStatus.INSTALLED_INACTIVE -> AppStrings.get("status_inactive", lang)
        EngineHealthStatus.DENIED -> AppStrings.get("status_denied", lang)
        EngineHealthStatus.NOT_INSTALLED -> AppStrings.get("status_missing", lang)
        EngineHealthStatus.ERROR -> AppStrings.get("status_error", lang)
    }
    val coreStatusColor = when (coreStatus) {
        EngineHealthStatus.ACTIVE -> NeonGreen
        EngineHealthStatus.AVAILABLE -> MaterialTheme.colorScheme.primary
        EngineHealthStatus.PERMISSION_REQUIRED, EngineHealthStatus.INSTALLED_INACTIVE -> SunsetAmber
        EngineHealthStatus.DENIED, EngineHealthStatus.ERROR -> CrimsonRed
        EngineHealthStatus.NOT_INSTALLED -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val coreStatusBackground = coreStatusColor.copy(alpha = 0.15f)

    var showLanguageBottomSheet by remember { mutableStateOf(false) }
    var showCustomHexBottomSheet by remember { mutableStateOf(false) }
    var showHowToUseBottomSheet by remember { mutableStateOf(false) }
    var showSupportBottomSheet by remember { mutableStateOf(false) }

    val colorPresets = remember {
        listOf(
            ColorPresetItem("color_sapphire", "ياقوتي", "#2563EB", SapphireBlue),
            ColorPresetItem("color_emerald", "زمردي", "#059669", EmeraldGreen),
            ColorPresetItem("color_amethyst", "جمشت", "#8B5CF6", AmethystPurple),
            ColorPresetItem("color_sunset", "غروب", "#F59E0B", SunsetAmber),
            ColorPresetItem("color_crimson", "قرمزي", "#EF4444", CrimsonRed),
            ColorPresetItem("color_teal", "فيروزي", "#14B8A6", SiwaneTeal)
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("settings_screen_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ==========================================
        // CARD 1: Language Selector Card ("لغة التطبيق")
        // ==========================================
        item(key = "card_language") {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLanguageBottomSheet = true }
                    .testTag("card_language_selector")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Start: Icon + Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Text(
                            text = AppStrings.get("settings_language", lang),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // End: Current Language + Chevron Down
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = currentAppLanguage.nativeName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // ==========================================
        // CARD 2: Appearance & Design ("المظهر والتصميم")
        // ==========================================
        item(key = "card_appearance") {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Header: Palette Icon + Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                        Text(
                            text = AppStrings.get("settings_appearance", lang),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // ------------------------------------------
                    // Subsection 1: وضع العرض (Display Mode)
                    // ------------------------------------------
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = AppStrings.get("settings_display_mode", lang),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Pill Segmented Control
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                SegmentButton(
                                    label = AppStrings.get("theme_dark", lang),
                                    selected = appSettings.themeMode == AppThemeMode.DARK,
                                    onClick = { settingsManager.updateThemeMode(AppThemeMode.DARK) },
                                    modifier = Modifier.weight(1f)
                                )
                                SegmentButton(
                                    label = AppStrings.get("theme_light", lang),
                                    selected = appSettings.themeMode == AppThemeMode.LIGHT,
                                    onClick = { settingsManager.updateThemeMode(AppThemeMode.LIGHT) },
                                    modifier = Modifier.weight(1f)
                                )
                                SegmentButton(
                                    label = AppStrings.get("theme_system", lang),
                                    selected = appSettings.themeMode == AppThemeMode.SYSTEM,
                                    onClick = { settingsManager.updateThemeMode(AppThemeMode.SYSTEM) },
                                    modifier = Modifier.weight(1.3f)
                                )
                            }
                        }
                    }

                    // ------------------------------------------
                    // Subsection 2: لوحة الألوان (Color Palette)
                    // ------------------------------------------
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = AppStrings.get("settings_color_palette", lang),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // 6 Color Chips in 2 rows of 3
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Row 1: Sapphire, Emerald, Amethyst
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for (preset in colorPresets.take(3)) {
                                    ColorPaletteChip(
                                        preset = preset,
                                        selected = !appSettings.dynamicSystemColor && appSettings.accentColorHex.equals(preset.hex, ignoreCase = true),
                                        onClick = {
                                            settingsManager.updateAccentColor(preset.hex, dynamicColor = false)
                                        },
                                        lang = lang,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            // Row 2: Sunset, Crimson, Siwane
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for (preset in colorPresets.drop(3)) {
                                    ColorPaletteChip(
                                        preset = preset,
                                        selected = !appSettings.dynamicSystemColor && appSettings.accentColorHex.equals(preset.hex, ignoreCase = true),
                                        onClick = {
                                            settingsManager.updateAccentColor(preset.hex, dynamicColor = false)
                                        },
                                        lang = lang,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // 2 Wide Pill Action Buttons below the chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            WideOptionButton(
                                label = AppStrings.get("settings_dynamic_m3", lang),
                                icon = Icons.Default.ColorLens,
                                selected = appSettings.dynamicSystemColor,
                                onClick = {
                                    settingsManager.updateAccentColor(appSettings.accentColorHex, dynamicColor = !appSettings.dynamicSystemColor)
                                },
                                modifier = Modifier.weight(1f)
                            )
                            WideOptionButton(
                                label = AppStrings.get("hex_custom_button", lang),
                                icon = Icons.Default.Palette,
                                selected = !appSettings.dynamicSystemColor && !colorPresets.any { it.hex.equals(appSettings.accentColorHex, ignoreCase = true) },
                                onClick = { showCustomHexBottomSheet = true },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // ------------------------------------------
                    // Subsection 3: مقياس الواجهة والخطوط (UI Scale)
                    // ------------------------------------------
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = AppStrings.get("settings_ui_scale_title", lang),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = AppStrings.get("settings_ui_scale_desc", lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Custom Slider with dynamic percentage display
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Slider(
                                value = appSettings.uiScalePercent.toFloat(),
                                onValueChange = { newVal ->
                                    settingsManager.updateUiScale(newVal.toInt())
                                },
                                valueRange = 80f..120f,
                                steps = 3, // 80, 90, 100, 110, 120
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    activeTickColor = Color.White,
                                    inactiveTickColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                Text(
                                    text = "${appSettings.uiScalePercent}%",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // CARD 3: Privilege & Engine Status (Account/Core Auth Card)
        // ==========================================
        item(key = "card_engine_status") {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icon + Title
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = AppStrings.get("engine_core_title", lang),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${AppStrings.get("engine_prefix", lang)} ${uiState.selectedEngine.shortName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Status Badge: synchronized with the selected engine's real health.
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = coreStatusBackground
                        ) {
                            Text(
                                text = coreStatusLabel,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = coreStatusColor
                            )
                        }
                    }

                    Text(
                        text = AppStrings.get("engine_status_desc", lang),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    // Real diagnostic action: refreshes both the selected engine health and
                    // the Android permission state. The button visibly reflects the in-flight check.
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (uiState.isRefreshing) {
                            MaterialTheme.colorScheme.surfaceVariant
                        } else {
                            Color(0xFFFEE2E2).copy(alpha = 0.85f)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !uiState.isRefreshing) {
                                viewModel.refreshPermissionsAndLockState()
                                viewModel.refreshEngineStatus()
                            }
                            .testTag("engine_check_permissions_button")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = if (uiState.isRefreshing) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    Color(0xFFDC2626)
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (uiState.isRefreshing) {
                                    AppStrings.get("status_checking", lang)
                                } else {
                                    AppStrings.get("engine_check_button", lang)
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.isRefreshing) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    Color(0xFFDC2626)
                                }
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // CARD 7: Guide, Help & Support
        // ==========================================
        item(key = "card_guide_support") {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SettingsNavRow(
                        icon = Icons.Default.HelpOutline,
                        title = AppStrings.get("settings_how_to_use", lang),
                        subtitle = AppStrings.get("qs_tile_tip", lang),
                        onClick = { showHowToUseBottomSheet = true }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    SettingsNavRow(
                        icon = Icons.Default.SupportAgent,
                        title = AppStrings.get("settings_support", lang),
                        subtitle = AppStrings.get("settings_support_email", lang),
                        onClick = { showSupportBottomSheet = true }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Version Tag
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = CircleShape,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = AppStrings.get("settings_edition", lang),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = AppStrings.get("settings_version_info", lang),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Bottom Sheets
    if (showLanguageBottomSheet) {
        LanguagePickerBottomSheet(
            currentLanguageCode = lang,
            onLanguageSelected = { newLang ->
                settingsManager.updateLanguage(newLang)
            },
            onDismiss = { showLanguageBottomSheet = false }
        )
    }

    if (showCustomHexBottomSheet) {
        HexColorBottomSheet(
            currentHex = appSettings.accentColorHex,
            lang = lang,
            onApplyColor = { newHex ->
                settingsManager.updateAccentColor(newHex, dynamicColor = false)
            },
            onDismiss = { showCustomHexBottomSheet = false }
        )
    }

    if (showHowToUseBottomSheet) {
        HowToUseBottomSheet(
            onDismiss = { showHowToUseBottomSheet = false }
        )
    }

    if (showSupportBottomSheet) {
        SupportBottomSheet(
            onDismiss = { showSupportBottomSheet = false }
        )
    }
}

/**
 * Segment button for the Display Mode segmented control.
 * Shows a checkmark icon when selected, just like the screenshot!
 */
@Composable
private fun SegmentButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else Color.Transparent,
        modifier = modifier
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (selected) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Color chip with colored circle dot and label.
 * Displays bold border and soft tinted background when selected, exactly matching the screenshot!
 */
@Composable
private fun ColorPaletteChip(
    preset: ColorPresetItem,
    selected: Boolean,
    onClick: () -> Unit,
    lang: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(preset.color, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = AppStrings.get(preset.nameKey, lang).ifEmpty { preset.defaultName },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

/**
 * Wide pill action button below the color chips (for Dynamic M3 and Hex مخصص).
 */
@Composable
private fun WideOptionButton(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ClockChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun SettingsNavRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
