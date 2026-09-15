package me.elhizazi.Castix.privilege.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import me.elhizazi.Castix.privilege.ui.AppStrings
import me.elhizazi.Castix.ui.theme.parseHexColor
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

/**
 * Custom Theme Color Picker Bottom Sheet matching the screenshot:
 * - Centered title & subtitle instructions
 * - Interactive circular HSV Color Wheel (Drag & Tap)
 * - HEX code input field with formatted label
 * - Live Circular Color Swatch
 * - Full-width "تطبيق اللون" (Apply Color) action button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HexColorBottomSheet(
    currentHex: String,
    lang: String = "ar",
    onApplyColor: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusManager = LocalFocusManager.current

    var hexInput by remember {
        val clean = currentHex.trim().removePrefix("#").uppercase()
        mutableStateOf(if (clean.length == 6) "#$clean" else currentHex)
    }

    // Parse current color to obtain initial HSV
    val currentColor = parseHexColor(hexInput)
    val currentInt = AndroidColor.rgb(
        (currentColor.red * 255).toInt(),
        (currentColor.green * 255).toInt(),
        (currentColor.blue * 255).toInt()
    )
    val hsv = remember { FloatArray(3) }
    AndroidColor.colorToHSV(currentInt, hsv)

    var currentHue by remember { mutableStateOf(hsv[0]) }
    var currentSat by remember { mutableStateOf(hsv[1]) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = Modifier.testTag("hex_color_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = AppStrings.get("hex_sheet_title", lang),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle
            Text(
                text = AppStrings.get("hex_sheet_subtitle", lang),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.fillMaxWidth(0.9f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Interactive HSV Color Wheel
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(240.dp)
                    .testTag("color_wheel_picker")
            ) {
                ColorWheelCanvas(
                    selectedHue = currentHue,
                    selectedSat = currentSat,
                    onColorChanged = { newHue, newSat ->
                        currentHue = newHue
                        currentSat = newSat
                        val colorInt = AndroidColor.HSVToColor(floatArrayOf(newHue, newSat, 1.0f))
                        hexInput = String.format("#%06X", (0xFFFFFF and colorInt))
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // HEX Input and Color Preview Swatch Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // HEX text field
                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { input ->
                        val cleaned = input.trim().uppercase()
                        hexInput = if (cleaned.startsWith("#")) cleaned else "#$cleaned"
                        if (hexInput.length == 7) {
                            try {
                                val c = AndroidColor.parseColor(hexInput)
                                val tempHsv = FloatArray(3)
                                AndroidColor.colorToHSV(c, tempHsv)
                                currentHue = tempHsv[0]
                                currentSat = tempHsv[1]
                            } catch (_: Exception) {}
                        }
                    },
                    label = {
                        Text(
                            text = AppStrings.get("hex_input_label", lang),
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                    ),
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Start
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("hex_color_text_input")
                )

                // Large Color Circle Preview
                val activePreviewColor = parseHexColor(hexInput)
                Surface(
                    shape = CircleShape,
                    color = activePreviewColor,
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .size(54.dp)
                        .testTag("hex_color_preview_circle")
                ) {}
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Full-Width Primary Action Button: "تطبيق اللون"
            val activeColor = parseHexColor(hexInput)
            val activeColorInt = AndroidColor.rgb(
                (activeColor.red * 255).toInt(),
                (activeColor.green * 255).toInt(),
                (activeColor.blue * 255).toInt()
            )
            val isLightColor = ColorUtils.calculateLuminance(activeColorInt) > 0.55
            val buttonTextColor = if (isLightColor) Color.Black else Color.White

            Button(
                onClick = {
                    onApplyColor(hexInput)
                    onDismiss()
                },
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = activeColor,
                    contentColor = buttonTextColor
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_apply_custom_color")
            ) {
                Text(
                    text = AppStrings.get("btn_apply", lang),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/**
 * Custom Color Wheel Canvas:
 * - Sweeps 360 degrees of hue rainbow
 * - Blends radial gradient from white at center to transparent at edge
 * - Draws interactive thumb handle at (hue, saturation)
 */
@Composable
private fun ColorWheelCanvas(
    selectedHue: Float,
    selectedSat: Float,
    onColorChanged: (hue: Float, saturation: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .size(240.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    calculateColorAt(offset, size.width, size.height, onColorChanged)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    calculateColorAt(change.position, size.width, size.height, onColorChanged)
                }
            }
    ) {
        val radius = min(size.width, size.height) / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // 1. Rainbow Hue Sweep Gradient
        val sweepBrush = Brush.sweepGradient(
            colors = listOf(
                Color(0xFFFF0000), // Red
                Color(0xFFFFFF00), // Yellow
                Color(0xFF00FF00), // Green
                Color(0xFF00FFFF), // Cyan
                Color(0xFF0000FF), // Blue
                Color(0xFFFF00FF), // Magenta
                Color(0xFFFF0000)  // Red
            ),
            center = center
        )
        drawCircle(brush = sweepBrush, radius = radius, center = center)

        // 2. Radial Saturation Gradient (White at center to Transparent at edge)
        val radialBrush = Brush.radialGradient(
            colors = listOf(Color.White, Color.White.copy(alpha = 0f)),
            center = center,
            radius = radius
        )
        drawCircle(brush = radialBrush, radius = radius, center = center)

        // 3. Draw Selector Handle
        val rad = Math.toRadians(selectedHue.toDouble())
        val dist = (selectedSat.coerceIn(0f, 1f)) * radius
        val handleCenter = Offset(
            x = center.x + (dist * cos(rad)).toFloat(),
            y = center.y + (dist * sin(rad)).toFloat()
        )

        val selectedColorInt = AndroidColor.HSVToColor(floatArrayOf(selectedHue, selectedSat, 1.0f))
        val selectedColor = Color(selectedColorInt)

        // Outer white ring
        drawCircle(
            color = Color.White,
            radius = 14.dp.toPx(),
            center = handleCenter
        )
        // Subtle dark shadow outline
        drawCircle(
            color = Color.Black.copy(alpha = 0.25f),
            radius = 14.dp.toPx(),
            center = handleCenter,
            style = Stroke(width = 1.5.dp.toPx())
        )
        // Inner color circle
        drawCircle(
            color = selectedColor,
            radius = 10.dp.toPx(),
            center = handleCenter
        )
    }
}

private fun calculateColorAt(
    offset: Offset,
    width: Int,
    height: Int,
    onColorChanged: (hue: Float, saturation: Float) -> Unit
) {
    val centerX = width / 2f
    val centerY = height / 2f
    val radius = min(centerX, centerY)

    val dx = offset.x - centerX
    val dy = offset.y - centerY
    val dist = hypot(dx, dy).coerceAtMost(radius)

    var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
    if (angle < 0f) angle += 360f

    val sat = (dist / radius).coerceIn(0f, 1f)
    onColorChanged(angle, sat)
}
