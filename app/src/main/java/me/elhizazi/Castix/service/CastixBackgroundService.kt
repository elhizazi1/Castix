package me.elhizazi.Castix.service

import me.elhizazi.Castix.privilege.ui.AppStrings
import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import me.elhizazi.Castix.MainActivity
import me.elhizazi.Castix.R
import me.elhizazi.Castix.privilege.domain.repository.TargetAppsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Foreground service providing continuous background media playback protection,
 * persistent status bar notification, lock screen CPU WakeLock hold, and floating overlay trigger.
 */
class CastixBackgroundService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null
    private var screenStateReceiver: BroadcastReceiver? = null
    private var overlayManager: FloatingOverlayManager? = null

    companion object {
        private const val TAG = "CastixService"
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "castix_playback_channel"

        const val ACTION_START = "me.elhizazi.Castix.action.START"
        const val ACTION_STOP = "me.elhizazi.Castix.action.STOP"
        const val ACTION_REFRESH_OVERLAY = "me.elhizazi.Castix.action.REFRESH_OVERLAY"
        const val ACTION_BLACK_SCREEN = "me.elhizazi.Castix.action.BLACK_SCREEN"

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

        private val _isLockScreenActive = MutableStateFlow(false)
        val isLockScreenActive: StateFlow<Boolean> = _isLockScreenActive.asStateFlow()

        fun start(context: Context) {
            val intent = Intent(context, CastixBackgroundService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, CastixBackgroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        overlayManager = FloatingOverlayManager(this)
        createNotificationChannel()
        registerScreenStateReceiver()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                Log.d(TAG, "Stopping CastixBackgroundService by user action")
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_BLACK_SCREEN -> {
                Log.d(TAG, "Toggling Black Screen overlay from notification")
                BlackScreenOverlayManager.getInstance(this).toggleBlackScreen()
                return START_STICKY
            }
            ACTION_REFRESH_OVERLAY -> {
                refreshOverlayState()
                return START_STICKY
            }
            else -> {
                // START or regular start
                startForegroundNotification()
                _isServiceRunning.value = true
                refreshOverlayState()
                return START_STICKY
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        Log.d(TAG, "CastixBackgroundService destroying")
        _isServiceRunning.value = false
        _isLockScreenActive.value = false

        overlayManager?.detach()
        overlayManager = null

        unregisterScreenStateReceiver()
        releaseWakeLock()

        super.onDestroy()
    }

    private fun startForegroundNotification() {
        val notification = buildPersistentNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildPersistentNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, CastixBackgroundService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val blackScreenIntent = Intent(this, CastixBackgroundService::class.java).apply {
            action = ACTION_BLACK_SCREEN
        }
        val blackScreenPendingIntent = PendingIntent.getService(
            this,
            2,
            blackScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(AppStrings.getForContext(this, "notification_service_title"))
            .setContentText(AppStrings.getForContext(this, "notification_service_text"))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(openPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Visible on Secure Lock Screen
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(
                android.R.drawable.ic_lock_power_off,
                AppStrings.getForContext(this, "notification_action_black_screen_ui"),
                blackScreenPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop / إيقاف",
                stopPendingIntent
            )
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Castix Background Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active background playback bypass status and controls"
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    @SuppressLint("WakelockTimeout")
    private fun acquireWakeLock() {
        try {
            if (wakeLock == null) {
                val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
                wakeLock = powerManager?.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "Castix::BackgroundPlaybackWakeLock"
                )?.apply {
                    setReferenceCounted(false)
                    acquire()
                }
                Log.d(TAG, "Castix Partial WakeLock acquired for Lock Screen protection")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to acquire WakeLock", e)
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
                Log.d(TAG, "Castix Partial WakeLock released")
            }
            wakeLock = null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release WakeLock", e)
        }
    }

    private fun registerScreenStateReceiver() {
        if (screenStateReceiver != null) return

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }

        screenStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
                when (intent?.action) {
                    Intent.ACTION_SCREEN_OFF -> {
                        Log.d(TAG, "Screen OFF detected: Device locked/standby. Ensuring WakeLock holds.")
                        _isLockScreenActive.value = true
                        acquireWakeLock()
                    }
                    Intent.ACTION_SCREEN_ON -> {
                        val isLocked = keyguardManager?.isKeyguardLocked == true
                        _isLockScreenActive.value = isLocked
                        if (BlackScreenOverlayManager.isBlackScreenActive.value) {
                            BlackScreenOverlayManager.getInstance(this@CastixBackgroundService).reassertImmersiveOnWake()
                        }
                    }
                    Intent.ACTION_USER_PRESENT -> {
                        Log.d(TAG, "User unlocked device (USER_PRESENT)")
                        _isLockScreenActive.value = false
                    }
                }
            }
        }

        registerReceiver(screenStateReceiver, filter)
    }

    private fun unregisterScreenStateReceiver() {
        screenStateReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (_: Exception) {}
        }
        screenStateReceiver = null
    }

    fun refreshOverlayState() {
        val repo = TargetAppsRepository(this)
        if (repo.isFloatingButtonEnabled() && FloatingOverlayManager.canDrawOverlays(this)) {
            overlayManager?.attach()
        } else {
            overlayManager?.detach()
        }
    }
}
