package me.elhizazi.Castix.privilege.ui.components

import me.elhizazi.Castix.privilege.ui.AppStrings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.elhizazi.Castix.R
import me.elhizazi.Castix.privilege.domain.model.FloatingButtonMode
import me.elhizazi.Castix.ui.theme.CyberAmber
import me.elhizazi.Castix.ui.theme.NeonCyan
import me.elhizazi.Castix.ui.theme.NeonGreen

/**
 * Card allowing the user to configure the Floating Overlay Trigger Button,
 * choose between showing it only inside selected target apps or globally,
 * and select which media apps to activate on.
 */
@Composable
fun FloatingTriggerCard(
    isEnabled: Boolean,
    mode: FloatingButtonMode,
    selectedAppsCount: Int,
    hasOverlayPermission: Boolean,
    isAccessibilityEnabled: Boolean = false,
    onToggleEnabled: (Boolean) -> Unit,
    onSetMode: (FloatingButtonMode) -> Unit,
    onOpenAppsDialog: () -> Unit,
    onRequestOverlayPermission: () -> Unit,
    onAutoGrantViaEngine: () -> Unit,
    onRequestAccessibility: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("floating_trigger_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (isEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
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
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = AppStrings.text("floating_title"),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = AppStrings.text("floating_title"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(
                            text = if (isEnabled) {
                                    AppStrings.text("floating_status_active")
                                } else {
                                    AppStrings.text("floating_status_disabled")
                                },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isEnabled) NeonGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggleEnabled,
                    modifier = Modifier.testTag("floating_button_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            if (!hasOverlayPermission && isEnabled) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CyberAmber.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, CyberAmber.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = CyberAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = AppStrings.text("floating_perm_required_title"),
                                color = CyberAmber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onRequestOverlayPermission,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyberAmber,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = AppStrings.text("btn_grant_overlay"),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            OutlinedButton(
                                onClick = onAutoGrantViaEngine,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, CyberAmber),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = AppStrings.text("btn_auto_grant_engine"),
                                    fontSize = 11.sp,
                                    color = CyberAmber
                                )
                            }
                        }
                    }
                }
            }

            if (isEnabled) {
                Spacer(modifier = Modifier.height(14.dp))

                // Mode Selector: Only in selected apps vs Always
                Text(
                    text = AppStrings.text("floating_mode_label"),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Option 1: Only selected apps
                    val isSelectedApps = mode == FloatingButtonMode.ONLY_SELECTED_APPS
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelectedApps) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(
                            1.dp,
                            if (isSelectedApps) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSetMode(FloatingButtonMode.ONLY_SELECTED_APPS) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isSelectedApps) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = AppStrings.text("floating_mode_selected_apps"),
                                fontSize = 11.sp,
                                fontWeight = if (isSelectedApps) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelectedApps) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    }

                    // Option 2: Always Visible
                    val isAlways = mode == FloatingButtonMode.ALWAYS_VISIBLE
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isAlways) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(
                            1.dp,
                            if (isAlways) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSetMode(FloatingButtonMode.ALWAYS_VISIBLE) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isAlways) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = AppStrings.text("floating_mode_always_visible"),
                                fontSize = 11.sp,
                                fontWeight = if (isAlways) FontWeight.Bold else FontWeight.Medium,
                                color = if (isAlways) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Target Apps Selector Button
                FilledTonalButton(
                    onClick = onOpenAppsDialog,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_select_target_apps"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = buildString {
                            append(AppStrings.text("floating_btn_manage_apps"))
                            append(" (")
                            append(AppStrings.text("floating_selected_apps_count", selectedAppsCount))
                            append(")")
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Zero-Root Accessibility Service Assistant Banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isAccessibilityEnabled) NeonGreen.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    border = BorderStroke(
                        1.dp,
                        if (isAccessibilityEnabled) NeonGreen.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isAccessibilityEnabled) Icons.Default.Check else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (isAccessibilityEnabled) NeonGreen else CyberAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = AppStrings.text("accessibility_name"),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = AppStrings.text("floating_accessibility_tip_desc"),
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (!isAccessibilityEnabled) {
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = onRequestAccessibility,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(
                                    text = AppStrings.text("btn_enable_accessibility"),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
