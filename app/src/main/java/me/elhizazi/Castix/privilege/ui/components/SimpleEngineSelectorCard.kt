package me.elhizazi.Castix.privilege.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.elhizazi.Castix.privilege.domain.engine.RootEngine
import me.elhizazi.Castix.privilege.domain.model.EngineHealthStatus
import me.elhizazi.Castix.privilege.domain.model.EngineType
import me.elhizazi.Castix.privilege.domain.model.ServiceHealth
import me.elhizazi.Castix.privilege.ui.AppStrings
import me.elhizazi.Castix.ui.theme.CyberAmber
import me.elhizazi.Castix.ui.theme.CyberRed
import me.elhizazi.Castix.ui.theme.NeonGreen

/**
 * Premium, spacious Privilege Engine Selector.
 * Organized into vertical card sections to prevent any horizontal text or button overlap.
 */
@Composable
fun SimpleEngineSelectorCard(
    selectedEngine: EngineType,
    healthMap: Map<EngineType, ServiceHealth>,
    onSelectEngine: (EngineType) -> Unit,
    onRequestPermission: (EngineType) -> Unit,
    onShowEngineDetails: (EngineType) -> Unit,
    lang: String = "auto",
    modifier: Modifier = Modifier
) {
    // The header badge represents the ENGINE CURRENTLY SELECTED by Castix,
    // not merely any backend that happens to be active on the device.
    // This prevents Shizuku from remaining visible after switching to Root.
    val selectedEngineHealth = healthMap[selectedEngine]
    val selectedEngineIsActive = selectedEngineHealth?.status == EngineHealthStatus.ACTIVE
    val hasActiveEngine = selectedEngineIsActive

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("simple_engine_selector_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = AppStrings.get("engine_selector_title", lang),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = AppStrings.get("engine_selector_desc", lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Overall Active Indicator Badge
                Surface(
                    color = if (hasActiveEngine) NeonGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        1.dp,
                        if (hasActiveEngine) NeonGreen.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(
                                    if (hasActiveEngine) NeonGreen else Color.Gray,
                                    CircleShape
                                )
                        )
                        Text(
                            text = if (hasActiveEngine) selectedEngine.shortName else AppStrings.get("status_none_active", lang),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (hasActiveEngine) NeonGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Notice when no engine is active
            if (!hasActiveEngine) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    color = CyberAmber.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CyberAmber.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = CyberAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = AppStrings.get("engine_none_active_hint", lang),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Engine List Items
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                EngineType.entries.forEach { engine ->
                    val health = healthMap[engine]
                    val isUsable = health?.status == EngineHealthStatus.ACTIVE
                    val isSelected = engine == selectedEngine

                    EngineCardItem(
                        engine = engine,
                        health = health,
                        isSelected = isSelected,
                        isUsable = isUsable,
                        lang = lang,
                        onSelect = { onSelectEngine(engine) },
                        onAuthorize = { onRequestPermission(engine) },
                        onShowDetails = { onShowEngineDetails(engine) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EngineCardItem(
    engine: EngineType,
    health: ServiceHealth?,
    isSelected: Boolean,
    isUsable: Boolean,
    lang: String,
    onSelect: () -> Unit,
    onAuthorize: () -> Unit,
    onShowDetails: () -> Unit
) {
    val context = LocalContext.current
    val detectedRootManager = remember {
        RootEngine.getDetectedRootManager(context)
    }

    val icon = when (engine) {
        EngineType.SHIZUKU -> Icons.Default.Security
        EngineType.ROOT -> Icons.Default.Terminal
        EngineType.ACCESSIBILITY -> Icons.Default.AccessibilityNew
        EngineType.LSPOSED -> Icons.Default.Extension
        EngineType.LSPATCH -> Icons.Default.Layers
        EngineType.DHIZUKU -> Icons.Default.Shield
    }

    val status = health?.status ?: EngineHealthStatus.NOT_INSTALLED
    val (statusLabel, badgeBg, badgeText, badgeBorder) = when (status) {
        EngineHealthStatus.ACTIVE -> Quadruple(
            AppStrings.get("status_active", lang),
            NeonGreen.copy(alpha = 0.15f),
            NeonGreen,
            NeonGreen.copy(alpha = 0.4f)
        )
        EngineHealthStatus.AVAILABLE -> Quadruple(
            AppStrings.get("status_available", lang),
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        )
        EngineHealthStatus.PERMISSION_REQUIRED -> Quadruple(
            AppStrings.get("status_perm_needed", lang),
            CyberAmber.copy(alpha = 0.15f),
            CyberAmber,
            CyberAmber.copy(alpha = 0.4f)
        )
        EngineHealthStatus.INSTALLED_INACTIVE -> Quadruple(
            AppStrings.get("status_inactive", lang),
            CyberAmber.copy(alpha = 0.15f),
            CyberAmber,
            CyberAmber.copy(alpha = 0.4f)
        )
        EngineHealthStatus.DENIED -> Quadruple(
            AppStrings.get("status_denied", lang),
            CyberRed.copy(alpha = 0.15f),
            CyberRed,
            CyberRed.copy(alpha = 0.4f)
        )
        EngineHealthStatus.NOT_INSTALLED -> Quadruple(
            AppStrings.get("status_missing", lang),
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
        EngineHealthStatus.ERROR -> Quadruple(
            AppStrings.get("status_error", lang),
            CyberRed.copy(alpha = 0.15f),
            CyberRed,
            CyberRed.copy(alpha = 0.4f)
        )
    }

    val buttonText = when {
        engine == EngineType.ROOT && detectedRootManager != null -> {
            "${AppStrings.get("engine_btn_open_manager", lang)} (${detectedRootManager.displayName})"
        }
        status == EngineHealthStatus.NOT_INSTALLED -> {
            AppStrings.get("engine_btn_setup_guide", lang)
        }
        status == EngineHealthStatus.INSTALLED_INACTIVE -> {
            AppStrings.get("engine_btn_start_service", lang)
        }
        status == EngineHealthStatus.PERMISSION_REQUIRED -> {
            AppStrings.get("engine_btn_grant_perm", lang)
        }
        status == EngineHealthStatus.DENIED -> {
            AppStrings.get("engine_btn_retry", lang)
        }
        else -> {
            AppStrings.get("engine_btn_activate", lang)
        }
    }

    val btnColor = when {
        status == EngineHealthStatus.INSTALLED_INACTIVE -> CyberAmber
        status == EngineHealthStatus.DENIED -> CyberRed
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onSelect() }
            .testTag("engine_item_${engine.name.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            }
        ),
        border = BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else if (isUsable) {
                NeonGreen.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Row 1: Engine Icon, Name, Architecture, and Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = engine.getLocalizedName(lang),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = AppStrings.get(
                            when (engine) {
                                EngineType.SHIZUKU -> "engine_arch_shizuku"
                                EngineType.DHIZUKU -> "engine_arch_dhizuku"
                                EngineType.ROOT -> "engine_arch_root"
                                EngineType.LSPOSED -> "engine_arch_lsposed"
                                EngineType.LSPATCH -> "engine_arch_lspatch"
                                EngineType.ACCESSIBILITY -> "engine_arch_accessibility"
                            },
                            lang
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Real-time Status Badge
                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, badgeBorder)
                ) {
                    Text(
                        text = statusLabel,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                        color = badgeText
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Description text with comfortable reading height
            Text(
                text = engine.getLocalizedDescription(lang),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 17.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Row 3: Bottom Action & Selection Bar (Never overlaps)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Radio Button Selection
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onSelect() }
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = onSelect,
                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSelected) AppStrings.get("active_engine_label", lang) else AppStrings.get("status_available", lang),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }

                // Action / Info Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedIconButton(
                        onClick = onShowDetails,
                        modifier = Modifier.size(32.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = AppStrings.get("engine_sheet_title", lang),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (!isUsable) {
                        Button(
                            onClick = onAuthorize,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = btnColor),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = buttonText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (btnColor == CyberAmber) Color.Black else MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    } else {
                        Surface(
                            color = NeonGreen.copy(alpha = 0.15f),
                            shape = CircleShape,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = AppStrings.get("status_active", lang),
                                    tint = NeonGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
