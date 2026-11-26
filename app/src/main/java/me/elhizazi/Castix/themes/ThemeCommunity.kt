package me.elhizazi.Castix.themes

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.elhizazi.Castix.privilege.ui.AppAppSettings
import me.elhizazi.Castix.privilege.ui.AppStrings

internal object ThemeCommunityLinks {
    const val STORE = "https://t.me/Castix_bot/castix"
    const val CHANNEL = "https://t.me/Castix_cas"
    const val COMMUNITY = "https://t.me/+tLUwyvvpOwxmZGY0"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ThemeCommunity(settings: AppAppSettings) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    fun label(key: String) = AppStrings.get(key, settings.language)
    fun open(url: String) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            expanded = false
        } catch (_: Exception) {
            Toast.makeText(context, label("cas_link_error"), Toast.LENGTH_SHORT).show()
        }
    }
    OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Icon(Icons.Default.Layers, null)
        Spacer(Modifier.width(10.dp))
        Text(label("cas_more"), modifier = Modifier.weight(1f))
        Icon(Icons.Default.KeyboardArrowDown, null)
    }
    if (expanded) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(onDismissRequest = { expanded = false }, sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp) {
            Column(Modifier.fillMaxWidth().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(label("cas_community_title"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(label("cas_community_desc"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                CommunityLink(Icons.Default.Layers, label("cas_store"), label("cas_store_desc"), true) { open(ThemeCommunityLinks.STORE) }
                CommunityLink(Icons.Default.Send, label("cas_official_channel"), label("cas_channel_desc")) { open(ThemeCommunityLinks.CHANNEL) }
                CommunityLink(Icons.Default.People, label("cas_community_title"), label("cas_group_desc")) { open(ThemeCommunityLinks.COMMUNITY) }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun CommunityLink(icon: ImageVector, title: String, description: String, primary: Boolean = false, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        color = if (primary) colors.primaryContainer else colors.surfaceVariant.copy(alpha = 0.35f),
        contentColor = if (primary) colors.onPrimaryContainer else colors.onSurface,
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.4f))) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = colors.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.OpenInNew, null, modifier = Modifier.size(18.dp))
        }
    }
}
