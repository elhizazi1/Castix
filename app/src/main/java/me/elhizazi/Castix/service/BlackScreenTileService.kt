package me.elhizazi.Castix.service

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import me.elhizazi.Castix.MainActivity
import me.elhizazi.Castix.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Quick Settings (QS) Tile allowing the user to pull down Android's top status bar
 * and tap the "Castix Black Screen" tile to instantly black out the screen edge-to-edge
 * while audio/video continues playing in the background.
 */
@RequiresApi(Build.VERSION_CODES.N)
class BlackScreenTileService : TileService() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var observeJob: Job? = null

    override fun onStartListening() {
        super.onStartListening()
        updateTileState(BlackScreenOverlayManager.isBlackScreenActive.value)
        observeJob = serviceScope.launch {
            BlackScreenOverlayManager.isBlackScreenActive.collectLatest { isActive ->
                updateTileState(isActive)
            }
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        observeJob?.cancel()
        observeJob = null
    }

    override fun onClick() {
        super.onClick()
        val app = applicationContext
        if (!FloatingOverlayManager.canDrawOverlays(app)) {
            // If overlay permission is not granted, open main app
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                startActivityAndCollapse(pendingIntent)
            } else {
                @Suppress("DEPRECATION")
                startActivityAndCollapse(intent)
            }
            return
        }

        // Toggle Black Screen AMOLED Edge-to-Edge
        val manager = BlackScreenOverlayManager.getInstance(app)
        manager.toggleBlackScreen()
        updateTileState(BlackScreenOverlayManager.isBlackScreenActive.value)
    }

    private fun updateTileState(isActive: Boolean) {
        val tile = qsTile ?: return
        tile.state = if (isActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = if (isActive) "إلغاء الشاشة السوداء" else "إطفاء الشاشة"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = if (isActive) "AMOLED شغال" else "Castix"
        }
        tile.icon = Icon.createWithResource(this, R.drawable.ic_qs_black_screen)
        tile.updateTile()
    }
}
