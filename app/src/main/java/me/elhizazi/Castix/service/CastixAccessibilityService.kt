package me.elhizazi.Castix.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Standard Android Accessibility Service providing zero-root, zero-Shizuku
 * foreground application detection, elevated system keep-alive priority,
 * and instant trigger of the Castix Floating Button.
 */
class CastixAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "CastixAccessibility"

        @Volatile
        private var instance: CastixAccessibilityService? = null

        private val _isServiceConnected = MutableStateFlow(false)
        val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()

        private val _currentForegroundPackage = MutableStateFlow<String?>(null)
        val currentForegroundPackage: StateFlow<String?> = _currentForegroundPackage.asStateFlow()

        /**
         * Resolves the current active foreground package on demand using the active accessibility instance.
         */
        fun getActiveForegroundPackage(): String? {
            return instance?.findTopInteractivePackage() ?: _currentForegroundPackage.value
        }

        /**
         * Checks if CastixAccessibilityService is enabled in Android System Settings.
         */
        fun isAccessibilityEnabled(context: Context): Boolean {
            val expectedComponentName = ComponentName(context, CastixAccessibilityService::class.java).flattenToString()
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServices)
            while (colonSplitter.hasNext()) {
                val componentName = colonSplitter.next()
                if (componentName.equals(expectedComponentName, ignoreCase = true)) {
                    return true
                }
            }
            return false
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        _isServiceConnected.value = true
        Log.d(TAG, "CastixAccessibilityService connected successfully")

        // Ensure configuration is active with interactive window flags
        serviceInfo = serviceInfo?.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOWS_CHANGED or
                    AccessibilityEvent.TYPE_VIEW_FOCUSED or
                    AccessibilityEvent.TYPE_VIEW_CLICKED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 20
        } ?: AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOWS_CHANGED or
                    AccessibilityEvent.TYPE_VIEW_FOCUSED or
                    AccessibilityEvent.TYPE_VIEW_CLICKED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 20
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val eventType = event.eventType
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            eventType == AccessibilityEvent.TYPE_WINDOWS_CHANGED ||
            eventType == AccessibilityEvent.TYPE_VIEW_FOCUSED ||
            eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {

            val eventPkg = event.packageName?.toString()
            val windowPkg = findTopInteractivePackage()

            // Resolve real user application package:
            // Prefer eventPkg if it's a valid user package; otherwise query window hierarchy
            val targetPkg = when {
                eventPkg != null && !isTransientOrSystemPackage(eventPkg) -> eventPkg
                windowPkg != null && !isTransientOrSystemPackage(windowPkg) -> windowPkg
                else -> null
            }

            if (targetPkg != null) {
                _currentForegroundPackage.value = targetPkg
                // Notify floating overlay directly for instant zero-latency appearance
                FloatingOverlayManager.notifyForegroundPackageChanged(targetPkg)
            }
        }
    }

    fun findTopInteractivePackage(): String? {
        val windowList = try { windows } catch (_: Exception) { null }
        if (!windowList.isNullOrEmpty()) {
            for (win in windowList) {
                if (win.isFocused || win.isActive) {
                    val pkg = win.root?.packageName?.toString()
                    if (pkg != null && !isTransientOrSystemPackage(pkg)) {
                        return pkg
                    }
                }
            }
        }
        val rootPkg = try { rootInActiveWindow?.packageName?.toString() } catch (_: Exception) { null }
        if (rootPkg != null && !isTransientOrSystemPackage(rootPkg)) {
            return rootPkg
        }
        return null
    }

    private fun isTransientOrSystemPackage(pkg: String): Boolean {
        return pkg == "com.android.systemui" ||
                pkg == "android" ||
                pkg == "com.google.android.gms" ||
                pkg == "com.google.android.permissioncontroller" ||
                pkg == "com.android.permissioncontroller" ||
                pkg.contains("inputmethod", ignoreCase = true) ||
                pkg.contains("keyboard", ignoreCase = true) ||
                pkg.contains(".ime", ignoreCase = true) ||
                pkg == packageName
    }

    override fun onInterrupt() {
        Log.w(TAG, "CastixAccessibilityService interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        _isServiceConnected.value = false
        Log.d(TAG, "CastixAccessibilityService destroyed")
    }
}
