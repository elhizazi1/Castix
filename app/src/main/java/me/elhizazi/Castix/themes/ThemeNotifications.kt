package me.elhizazi.Castix.themes

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class ThemeNotifications : NotificationListenerService() {
    companion object {
        @Volatile var count: Int? = null
            private set
    }
    private fun refresh() { count = runCatching { activeNotifications.count { it.packageName != packageName && !it.isOngoing } }.getOrNull() }
    override fun onListenerConnected() { refresh() }
    override fun onListenerDisconnected() { count = null }
    override fun onNotificationPosted(sbn: StatusBarNotification) { refresh() }
    override fun onNotificationRemoved(sbn: StatusBarNotification) { refresh() }
    override fun onDestroy() { count = null; super.onDestroy() }
}
