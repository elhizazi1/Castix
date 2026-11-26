package me.elhizazi.Castix.themes

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.elhizazi.Castix.privilege.ui.AppAppSettings
import me.elhizazi.Castix.privilege.ui.AppSettingsManager
import me.elhizazi.Castix.privilege.ui.AppStrings

@Composable
internal fun ThemePanel(manager: AppSettingsManager, settings: AppAppSettings) {
    val context = LocalContext.current
    val store = remember { ThemeStore(context.applicationContext) }
    val scope = rememberCoroutineScope()
    var themes by remember { mutableStateOf(store.list()) }
    var pending by remember { mutableStateOf<Uri?>(null) }
    var source by remember { mutableStateOf<Uri?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    fun label(key: String) = AppStrings.get(key, settings.language)
    fun perform(action: () -> Unit) {
        busy = true
        scope.launch {
            try {
                withContext(Dispatchers.IO) { action() }
                themes = withContext(Dispatchers.IO) { store.list() }
                message = "cas_success"
            } catch (e: Exception) { message = "cas_error" }
            finally { busy = false }
        }
    }
    val save = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { dest ->
        val input = source; source = null
        if (dest != null && input != null) perform { store.convert(input, dest) }
    }
    val convert = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) { source = uri; save.launch("theme.cas") }
    }
    val import = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { pending = it }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        Text(label("cas_title"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(label("cas_desc"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        themes.forEach { theme ->
            val selected = settings.customThemeId == theme.id
            var menu by remember(theme.id) { mutableStateOf(false) }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth().clickable(enabled = !busy) { manager.selectCustomTheme(theme.id) }
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(42.dp)) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.AccessTime, null, tint = MaterialTheme.colorScheme.primary) }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(theme.name, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                        Text(if (selected) label("cas_active") else label("cas_select"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (selected) Icon(Icons.Default.Check, label("cas_active"), tint = MaterialTheme.colorScheme.primary)
                    Box {
                        IconButton(onClick = { menu = true }, enabled = !busy) { Icon(Icons.Default.MoreVert, label("cas_delete")) }
                        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                            DropdownMenuItem(text = { Text(label("cas_delete")) }, onClick = { menu = false; if (selected) manager.selectCustomTheme(null); perform { store.delete(theme.id) } })
                        }
                    }
                }
            }
        }
        ThemeAction(Icons.Default.Add, label("cas_import"), !busy) { import.launch(arrayOf("*/*")) }
        ThemeAction(Icons.Default.Layers, label("cas_convert"), !busy) { convert.launch(arrayOf("*/*")) }
        if (busy) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        message?.let { Text(label(it), style = MaterialTheme.typography.bodySmall) }
    }
    pending?.let { uri ->
        AlertDialog(onDismissRequest = { pending = null }, title = { Text(label("cas_import")) }, text = { Text(label("cas_trust")) }, confirmButton = { TextButton(onClick = { pending = null; perform { store.install(uri) } }) { Text(label("cas_install")) } }, dismissButton = { TextButton(onClick = { pending = null }) { Text(label("cas_cancel")) } })
    }
}

@Composable
internal fun ThemeAction(icon: ImageVector, title: String, enabled: Boolean = true, action: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = action).padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(14.dp))
        Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

