package me.elhizazi.Castix.privilege.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun AmbientDurationRow(settings: AppAppSettings, lang: String, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    fun durationLabel(seconds: Int): String = AppStrings.get("ambient_duration_value", lang, seconds)
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text(AppStrings.get("ambient_duration", lang), style = MaterialTheme.typography.titleSmall)
            Text(AppStrings.get("ambient_duration_desc", lang), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Box {
            OutlinedButton(onClick = { expanded = true }, enabled = settings.ambientAutoDim) {
                Text(durationLabel(settings.ambientDurationSeconds))
            }
            DropdownMenu(expanded = expanded && settings.ambientAutoDim, onDismissRequest = { expanded = false }) {
                ambientDurationOptions.forEach { seconds ->
                    DropdownMenuItem(text = { Text(durationLabel(seconds)) }, onClick = {
                        expanded = false
                        onSelect(seconds)
                    })
                }
            }
        }
    }
}
