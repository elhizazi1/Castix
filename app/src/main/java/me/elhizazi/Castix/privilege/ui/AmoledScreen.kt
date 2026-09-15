package me.elhizazi.Castix.privilege.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun AmoledScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val settingsManager = remember { AppSettingsManager.getInstance(context) }
    val appSettings by settingsManager.settings.collectAsState()
    val lang = appSettings.language

    val accentColor = try {
        Color(android.graphics.Color.parseColor(appSettings.accentColorHex))
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }

    var showInscriptionDialog by remember { mutableStateOf(false) }
    var inscriptionInput by remember { mutableStateOf(appSettings.customInscription) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header Banner
        item {
            Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(accentColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = AppStrings.get("save_power_badge", lang),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = AppStrings.get("black_screen_title", lang),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = AppStrings.get("black_screen_desc", lang),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 1. Interactive Live AMOLED Screen Preview Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = AppStrings.get("amoled_preview_title", lang),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = AppStrings.get("amoled_preview_desc", lang),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Phone frame mockup with pitch-black OLED background
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black)
                            .border(
                                width = 1.5.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        accentColor.copy(alpha = 0.8f),
                                        Color.Black,
                                        accentColor.copy(alpha = 0.4f)
                                    )
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Ambient clock and active widgets
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            val timeNow = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                            val dateNow = SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(Date())

                            when (appSettings.clockStyle) {
                                ClockStyle.LARGE_GLOW -> {
                                    Text(
                                        text = timeNow,
                                        fontSize = 46.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = accentColor,
                                        fontFamily = FontFamily.SansSerif
                                    )
                                }
                                ClockStyle.MINIMAL_COMPACT -> {
                                    Text(
                                        text = timeNow,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                ClockStyle.ANALOG_MINIMAL -> {
                                    Text(
                                        text = "●  $timeNow  ◯",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color.White
                                    )
                                }
                                ClockStyle.RETRO_FLIP -> {
                                    val parts = timeNow.split(":")
                                    val h = parts.getOrNull(0) ?: "12"
                                    val m = parts.getOrNull(1) ?: "00"
                                    Text(
                                        text = "[ $h : $m ]",
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = accentColor
                                    )
                                }
                                ClockStyle.TEXT_WORD -> {
                                    Text(
                                        text = AppStrings.text("amoled_preview_time", timeNow),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                ClockStyle.PIXEL_AIRY -> {
                                    Text(timeNow, fontSize = 38.sp, fontWeight = FontWeight.Light, letterSpacing = 2.sp, color = Color.White)
                                }
                                ClockStyle.MATERIAL_EXPRESSIVE -> {
                                    Surface(shape = RoundedCornerShape(24.dp), color = accentColor.copy(alpha = 0.14f)) {
                                        Text(timeNow, fontSize = 32.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1.5).sp, color = Color.White, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                                    }
                                }
                                ClockStyle.LARGE_CENTER -> {
                                    Text(timeNow, fontSize = 48.sp, fontWeight = FontWeight.Black, letterSpacing = (-2.5).sp, color = Color.White)
                                }
                                ClockStyle.STACKED -> {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(timeNow.substringBefore(":"), fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(timeNow.substringAfter(":"), fontSize = 34.sp, fontWeight = FontWeight.Bold, color = accentColor)
                                    }
                                }
                                ClockStyle.MONO_LINE -> {
                                    Text(timeNow, fontSize = 18.sp, fontFamily = FontFamily.Monospace, letterSpacing = 2.sp, color = Color.White)
                                }
                                ClockStyle.DATA_DASHBOARD -> {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(timeNow, fontSize = 31.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Spacer(Modifier.height(5.dp))
                                        Text("•  •  •", fontSize = 10.sp, color = accentColor)
                                    }
                                }
                                else -> {
                                    Text(
                                        text = timeNow,
                                        fontSize = 38.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            if (appSettings.showDateOnBlackScreen) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = dateNow,
                                    fontSize = 12.sp,
                                    color = Color.LightGray
                                )
                            }

                            if (appSettings.showBatteryOnBlackScreen) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = AppStrings.text("amoled_preview_battery"),
                                    fontSize = 11.sp,
                                    color = Color(0xFF4ADE80),
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            if (appSettings.showPlaybackTimer) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = AppStrings.text("amoled_preview_session"),
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }

                            if (appSettings.showAppBadgeOnBlackScreen) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = accentColor.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = AppStrings.text("amoled_preview_now_playing"),
                                        fontSize = 10.sp,
                                        color = accentColor,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            if (appSettings.customInscription.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = appSettings.customInscription,
                                    fontSize = 10.sp,
                                    color = Color.Gray.copy(alpha = 0.8f)
                                )
                            }
                        }

                        // Bottom double tap hint
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .alpha(0.35f)
                        ) {
                            Text(
                                text = AppStrings.get("black_screen_unlock_hint", lang),
                                fontSize = 10.sp,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick Action Button to Test Black Screen Immediately
                    Button(
                        onClick = { viewModel.toggleBlackScreen() },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.isBlackScreenActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = if (uiState.isBlackScreenActive) Icons.Default.Visibility else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.isBlackScreenActive)
                                AppStrings.get("black_screen_action_off", lang)
                            else
                                AppStrings.get("amoled_btn_test", lang),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. Clock Styles & Design Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = AppStrings.get("amoled_clock_section", lang),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = AppStrings.get("amoled_clock_section_desc", lang),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Swipeable template carousel. The template names are localized; the cards
                    // themselves are visual previews so no language-specific copy is baked into them.
                    val clockOptions = listOf(
                        ClockStyle.DIGITAL_CLEAN to "clock_style_digital",
                        ClockStyle.MINIMAL_COMPACT to "clock_style_minimal",
                        ClockStyle.LARGE_GLOW to "clock_style_glow",
                        ClockStyle.ANALOG_MINIMAL to "clock_style_analog",
                        ClockStyle.RETRO_FLIP to "clock_style_retro",
                        ClockStyle.TEXT_WORD to "clock_style_text_word",
                        ClockStyle.PIXEL_AIRY to "clock_style_pixel_airy",
                        ClockStyle.MATERIAL_EXPRESSIVE to "clock_style_material_expressive",
                        ClockStyle.LARGE_CENTER to "clock_style_large_center",
                        ClockStyle.STACKED to "clock_style_stacked",
                        ClockStyle.MONO_LINE to "clock_style_mono_line",
                        ClockStyle.DATA_DASHBOARD to "clock_style_dashboard"
                    )
                    val selectedIndex = clockOptions.indexOfFirst { it.first == appSettings.clockStyle }.coerceAtLeast(0)

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(clockOptions.size) { index ->
                                val (style, nameKey) = clockOptions[index]
                                val isSelected = appSettings.clockStyle == style
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Black,
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
                                    ),
                                    modifier = Modifier
                                        .width(190.dp)
                                        .clickable { settingsManager.updateClockStyle(style) }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        AmoledTemplateMiniPreview(
                                            style = style,
                                            accentColor = accentColor,
                                            scalePercent = appSettings.clockScalePercent,
                                            modifier = Modifier.fillMaxWidth().height(120.dp)
                                        )
                                        Spacer(modifier = Modifier.height(9.dp))
                                        Text(
                                            text = AppStrings.get(nameKey, lang),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        Slider(
                            value = selectedIndex.toFloat(),
                            onValueChange = { index ->
                                val target = clockOptions[index.roundToInt().coerceIn(clockOptions.indices)]
                                settingsManager.updateClockStyle(target.first)
                            },
                            valueRange = 0f..(clockOptions.lastIndex.toFloat()),
                            steps = (clockOptions.size - 2).coerceAtLeast(0),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Professional per-style tuning: these values are persisted and also
                        // applied by the real AMOLED overlay, not only to the preview.
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = AppStrings.get("clock_customization_title", lang),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = AppStrings.get("clock_customization_desc", lang),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = "${AppStrings.get("clock_scale_label", lang)}  ${appSettings.clockScalePercent}%",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Slider(
                                value = appSettings.clockScalePercent.toFloat(),
                                onValueChange = { settingsManager.updateClockScale(it.roundToInt()) },
                                valueRange = 75f..140f,
                                steps = 12
                            )

                            Text(
                                text = "${AppStrings.get("clock_position_label", lang)}  ${appSettings.clockVerticalOffsetDp}dp",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Slider(
                                value = appSettings.clockVerticalOffsetDp.toFloat(),
                                onValueChange = { settingsManager.updateClockVerticalOffset(it.roundToInt()) },
                                valueRange = -80f..80f,
                                steps = 15
                            )

                            Text(
                                text = "${AppStrings.get("clock_spacing_label", lang)}  ${appSettings.clockSpacingPercent}%",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Slider(
                                value = appSettings.clockSpacingPercent.toFloat(),
                                onValueChange = { settingsManager.updateClockSpacing(it.roundToInt()) },
                                valueRange = 60f..150f,
                                steps = 8
                            )
                        }
                    }
                }
            }
        }

        // 3. Displayed Screen Elements Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Widgets,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = AppStrings.get("amoled_display_elements", lang),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = AppStrings.get("amoled_display_elements_desc", lang),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Battery status switch
                    AmoledSwitchRow(
                        icon = Icons.Default.BatteryChargingFull,
                        title = AppStrings.get("show_battery_label", lang),
                        description = AppStrings.get("show_battery_desc", lang),
                        checked = appSettings.showBatteryOnBlackScreen,
                        onCheckedChange = { settingsManager.updateShowBattery(it) }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    // Date and day switch
                    AmoledSwitchRow(
                        icon = Icons.Default.CalendarToday,
                        title = AppStrings.get("show_date_label", lang),
                        description = AppStrings.get("show_date_desc", lang),
                        checked = appSettings.showDateOnBlackScreen,
                        onCheckedChange = { settingsManager.updateShowDate(it) }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    // App badge switch
                    AmoledSwitchRow(
                        icon = Icons.Default.Layers,
                        title = AppStrings.get("show_app_badge_label", lang),
                        description = AppStrings.get("show_app_badge_desc", lang),
                        checked = appSettings.showAppBadgeOnBlackScreen,
                        onCheckedChange = { settingsManager.updateShowAppBadge(it) }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    // Playback timer switch
                    AmoledSwitchRow(
                        icon = Icons.Default.HourglassBottom,
                        title = AppStrings.get("show_playback_timer_label", lang),
                        description = AppStrings.get("show_playback_timer_desc", lang),
                        checked = appSettings.showPlaybackTimer,
                        onCheckedChange = { settingsManager.updateShowPlaybackTimer(it) }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    // Burn-in pixel shifting protection
                    AmoledSwitchRow(
                        icon = Icons.Default.Shield,
                        title = AppStrings.get("burn_in_label", lang),
                        description = AppStrings.get("burn_in_desc", lang),
                        checked = appSettings.burnInProtection,
                        onCheckedChange = { settingsManager.updateBurnInProtection(it) }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    // Double Tap to Wake
                    AmoledSwitchRow(
                        icon = Icons.Default.TouchApp,
                        title = AppStrings.get("settings_double_tap_wake", lang),
                        description = AppStrings.get("settings_double_tap_wake_desc", lang),
                        checked = uiState.blackScreenDoubleTap,
                        onCheckedChange = { viewModel.setBlackScreenDoubleTap(it) }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    // Zero Brightness
                    AmoledSwitchRow(
                        icon = Icons.Default.BrightnessLow,
                        title = AppStrings.get("settings_zero_brightness", lang),
                        description = AppStrings.get("settings_zero_brightness_desc", lang),
                        checked = uiState.blackScreenZeroBrightness,
                        onCheckedChange = { viewModel.setBlackScreenZeroBrightness(it) }
                    )
                }
            }
        }

        // 4. Custom Inscription / Signature Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = AppStrings.get("custom_inscription_label", lang),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = AppStrings.get("custom_inscription_desc", lang),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = inscriptionInput,
                        onValueChange = {
                            inscriptionInput = it
                            settingsManager.updateCustomInscription(it)
                        },
                        placeholder = {
                            Text(
                                text = AppStrings.get("custom_inscription_hint", lang),
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        singleLine = true
                    )
                }
            }
        }

        // 5. Floating Button Customization Card (Transferred from Settings)
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Layers,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = AppStrings.get("floating_customization_section", lang),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = AppStrings.get("floating_customization_desc", lang),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Live Interactive Floating Disc Visualizer inside the card!
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                RoundedCornerShape(18.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val discAlpha by animateFloatAsState(
                            targetValue = appSettings.floatingButtonAlphaPercent / 100f,
                            label = "discAlpha"
                        )
                        // The actual floating disc visual
                        Box(
                            modifier = Modifier
                                .size(appSettings.floatingButtonSizeDp.dp)
                                .alpha(discAlpha)
                                .border(
                                    width = (appSettings.floatingButtonSizeDp * 0.08f).coerceAtLeast(3f).dp,
                                    color = accentColor,
                                    shape = CircleShape
                                )
                                .background(
                                    color = Color.Black.copy(alpha = 0.45f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size((appSettings.floatingButtonSizeDp * 0.28f).dp)
                                    .background(accentColor, CircleShape)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 1. Floating button size slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppStrings.get("floating_size_label", lang),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${appSettings.floatingButtonSizeDp} dp",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Slider(
                        value = appSettings.floatingButtonSizeDp.toFloat(),
                        onValueChange = { settingsManager.updateFloatingButtonSize(it.toInt()) },
                        valueRange = 36f..76f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 2. Floating button alpha slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppStrings.get("floating_alpha_label", lang),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${appSettings.floatingButtonAlphaPercent}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Slider(
                        value = appSettings.floatingButtonAlphaPercent.toFloat(),
                        onValueChange = { settingsManager.updateFloatingButtonAlpha(it.toInt()) },
                        valueRange = 20f..100f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 3. Haptic feedback on tap
                    AmoledSwitchRow(
                        icon = Icons.Default.Vibration,
                        title = AppStrings.get("floating_haptic_label", lang),
                        description = AppStrings.get("floating_haptic_desc", lang),
                        checked = appSettings.floatingHapticFeedback,
                        onCheckedChange = { settingsManager.updateFloatingHapticFeedback(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AmoledTemplateMiniPreview(
    style: ClockStyle,
    accentColor: Color,
    scalePercent: Int = 100,
    modifier: Modifier = Modifier
) {
    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    val parts = time.split(":")
    val hour = parts.getOrElse(0) { "12" }
    val minute = parts.getOrElse(1) { "34" }
    val scaleFactor = (scalePercent / 100f).coerceIn(0.75f, 1.4f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Black)
            .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.scale(scaleFactor), contentAlignment = Alignment.Center) {
            when (style) {
                ClockStyle.DIGITAL_CLEAN ->
                    Text(time, fontSize = 30.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-1.2).sp, color = Color.White)
                ClockStyle.MINIMAL_COMPACT ->
                    Text(time, fontSize = 23.sp, fontWeight = FontWeight.Medium, letterSpacing = (-0.8).sp, color = Color.White)
                ClockStyle.LARGE_GLOW ->
                    Text(time, fontSize = 38.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-2).sp, color = accentColor)
                ClockStyle.ANALOG_MINIMAL ->
                    Text("◷  $time", fontSize = 24.sp, fontFamily = FontFamily.Monospace, color = Color.White)
                ClockStyle.RETRO_FLIP ->
                    Text("$hour : $minute", fontSize = 24.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color.White)
                ClockStyle.TEXT_WORD ->
                    Text("$hour:$minute", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                ClockStyle.PIXEL_AIRY ->
                    Text(time, fontSize = 35.sp, fontWeight = FontWeight.Light, letterSpacing = 2.sp, color = Color.White)
                ClockStyle.MATERIAL_EXPRESSIVE ->
                    Surface(shape = RoundedCornerShape(22.dp), color = accentColor.copy(alpha = 0.13f)) {
                        Text(time, fontSize = 31.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1.5).sp, color = Color.White, modifier = Modifier.padding(horizontal = 15.dp, vertical = 8.dp))
                    }
                ClockStyle.LARGE_CENTER ->
                    Text(time, fontSize = 44.sp, fontWeight = FontWeight.Black, letterSpacing = (-2.5).sp, color = Color.White)
                ClockStyle.STACKED ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(hour, fontSize = 31.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(minute, fontSize = 31.sp, fontWeight = FontWeight.Bold, color = accentColor)
                    }
                ClockStyle.MONO_LINE ->
                    Text("$hour:$minute", fontSize = 17.sp, fontFamily = FontFamily.Monospace, letterSpacing = 2.sp, color = Color.White)
                ClockStyle.DATA_DASHBOARD ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(time, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(Modifier.height(5.dp))
                        Surface(shape = RoundedCornerShape(50), color = accentColor.copy(alpha = 0.12f)) {
                            Text("•  •  •", fontSize = 10.sp, color = accentColor, modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp))
                        }
                    }
            }
        }
    }
}

@Composable
private fun AmoledSwitchRow(
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
