package me.elhizazi.Castix.privilege.ui.components

import me.elhizazi.Castix.privilege.ui.AppStrings
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.elhizazi.Castix.R
import me.elhizazi.Castix.privilege.domain.model.EngineHealthStatus
import me.elhizazi.Castix.privilege.domain.model.EngineType
import me.elhizazi.Castix.privilege.domain.model.ServiceHealth
import me.elhizazi.Castix.ui.theme.CyberAmber
import me.elhizazi.Castix.ui.theme.CyberRed
import me.elhizazi.Castix.ui.theme.NeonGreen

@Composable
fun ServiceStatusRow(
    healthMap: Map<EngineType, ServiceHealth>,
    onEngineClick: (EngineType) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("service_status_container"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
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
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = AppStrings.text("service_status_title"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = AppStrings.text("live_ipc_monitors"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EngineType.entries.forEach { engineType ->
                    val health = healthMap[engineType] ?: ServiceHealth(
                        type = engineType,
                        status = EngineHealthStatus.NOT_INSTALLED,
                        message = "Checking..."
                    )
                    ServiceStatusChip(
                        health = health,
                        onClick = { onEngineClick(engineType) }
                    )
                }
            }
        }
    }
}

@Composable
fun ServiceStatusChip(
    health: ServiceHealth,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusText = when (health.status) {
        EngineHealthStatus.ACTIVE -> AppStrings.text("status_active")
        EngineHealthStatus.AVAILABLE -> AppStrings.text("status_available")
        EngineHealthStatus.PERMISSION_REQUIRED -> AppStrings.text("status_perm_required")
        EngineHealthStatus.DENIED -> AppStrings.text("status_denied")
        EngineHealthStatus.INSTALLED_INACTIVE -> AppStrings.text("status_inactive")
        EngineHealthStatus.NOT_INSTALLED -> AppStrings.text("status_missing")
        EngineHealthStatus.ERROR -> AppStrings.text("status_error")
    }

    val (statusColor, statusIcon) = when (health.status) {
        EngineHealthStatus.ACTIVE -> Pair(NeonGreen, Icons.Default.CheckCircle)
        EngineHealthStatus.AVAILABLE -> Pair(MaterialTheme.colorScheme.primary, Icons.Default.RadioButtonUnchecked)
        EngineHealthStatus.PERMISSION_REQUIRED -> Pair(CyberAmber, Icons.Default.Lock)
        EngineHealthStatus.DENIED -> Pair(CyberRed, Icons.Default.Error)
        EngineHealthStatus.INSTALLED_INACTIVE -> Pair(CyberAmber, Icons.Default.Warning)
        EngineHealthStatus.NOT_INSTALLED -> Pair(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), Icons.Default.RadioButtonUnchecked)
        EngineHealthStatus.ERROR -> Pair(CyberRed, Icons.Default.Error)
    }

    val labelText = "${health.type.shortName}: $statusText"

    val animatedBorderColor by animateColorAsState(targetValue = statusColor, label = "statusBorder")

    Card(
        onClick = onClick,
        modifier = modifier.testTag("status_chip_${health.type.name.lowercase()}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        ),
        border = BorderStroke(1.dp, animatedBorderColor.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pulsing dot indicator
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
            ) {
                Surface(
                    color = statusColor,
                    shape = CircleShape,
                    modifier = Modifier.size(8.dp)
                ) {}
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = statusIcon,
                contentDescription = labelText,
                tint = statusColor,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = labelText,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
