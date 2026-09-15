package me.elhizazi.Castix.service

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import me.elhizazi.Castix.MainActivity
import me.elhizazi.Castix.R
import me.elhizazi.Castix.privilege.domain.model.FloatingButtonMode
import me.elhizazi.Castix.privilege.domain.repository.TargetAppsRepository
import me.elhizazi.Castix.privilege.ui.AppSettingsManager
import kotlin.math.abs

/**
 * Manages the floating trigger button displayed on top of other applications.
 * Adheres to selected target applications whitelist with anti-flicker debouncing,
 * supports smooth dragging, custom sizing & opacity, and one-tap instant screen off.
 */
class FloatingOverlayManager(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
    private val repository = TargetAppsRepository(context)
    private var overlayView: View? = null
    private var isAttached = false

    private val mainHandler = Handler(Looper.getMainLooper())
    private var appCheckRunnable: Runnable? = null

    // Anti-flicker debounce state
    private var lastObservedPackage: String? = null
    private var lastDecisionVisible: Boolean = false
    private var pendingHideRunnable: Runnable? = null

    companion object {
        private const val TAG = "FloatingOverlayManager"
        private var activeInstance: FloatingOverlayManager? = null

        @Volatile
        private var lastForegroundPackage: String? = null

        fun notifyForegroundPackageChanged(packageName: String) {
            lastForegroundPackage = packageName
            activeInstance?.let { manager ->
                manager.mainHandler.post {
                    manager.onPackageChanged(packageName)
                }
            }
        }

        fun refreshCurrentVisibility() {
            activeInstance?.let { manager ->
                manager.mainHandler.post {
                    manager.applyCurrentVisibility()
                }
            }
        }

        fun getActiveForegroundPackage(): String? {
            return lastForegroundPackage ?: activeInstance?.getForegroundPackageName()
        }

        fun canDrawOverlays(context: Context): Boolean {
            return Settings.canDrawOverlays(context)
        }

        fun hasUsageStatsPermission(context: Context): Boolean {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
                ?: return false
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            } else {
                @Suppress("DEPRECATION")
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            }
            return mode == AppOpsManager.MODE_ALLOWED
        }

        fun requestUsageStatsPermission(context: Context) {
            try {
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Cannot launch usage access settings", e)
            }
        }

        fun requestOverlayPermission(context: Context) {
            try {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:${context.packageName}")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Cannot launch overlay permission screen", e)
            }
        }
    }

    fun attach() {
        if (isAttached || windowManager == null) return
        if (!Settings.canDrawOverlays(context)) {
            Log.w(TAG, "Cannot attach overlay: SYSTEM_ALERT_WINDOW permission missing")
            return
        }

        try {
            val layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 40
                y = 450
            }

            val view = buildFloatingPillView()
            setupTouchHandling(view, layoutParams)

            windowManager.addView(view, layoutParams)
            overlayView = view
            isAttached = true
            activeInstance = this

            startForegroundAppMonitoring()
            Log.d(TAG, "Castix Floating Overlay successfully attached")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to attach floating overlay", e)
            isAttached = false
            overlayView = null
            activeInstance = null
        }
    }

    fun detach() {
        stopForegroundAppMonitoring()
        pendingHideRunnable?.let { mainHandler.removeCallbacks(it) }
        pendingHideRunnable = null

        if (isAttached && overlayView != null && windowManager != null) {
            try {
                windowManager.removeView(overlayView)
            } catch (e: Exception) {
                Log.e(TAG, "Error removing overlay view", e)
            }
        }
        overlayView = null
        isAttached = false
        activeInstance = null
    }

    fun onPackageChanged(pkg: String) {
        val view = overlayView ?: return
        if (!isAttached) return

        // Skip internal/transient system packages (keyboard, systemui, play services, dialogs)
        if (isTransientOrSystemUiPackage(pkg)) {
            return
        }

        lastObservedPackage = pkg
        evaluateVisibility(pkg)
    }

    fun applyCurrentVisibility() {
        val view = overlayView ?: return
        if (!isAttached) return

        val mode = repository.getFloatingButtonMode()
        if (mode == FloatingButtonMode.ALWAYS_VISIBLE) {
            pendingHideRunnable?.let { mainHandler.removeCallbacks(it) }
            pendingHideRunnable = null
            view.visibility = View.VISIBLE
            lastDecisionVisible = true
            return
        }

        // ONLY_SELECTED_APPS mode: check current active package
        val activePkg = lastForegroundPackage ?: getForegroundPackageName()
        val selectedPackages = repository.getSelectedPackages()

        if (activePkg != null && selectedPackages.contains(activePkg)) {
            pendingHideRunnable?.let { mainHandler.removeCallbacks(it) }
            pendingHideRunnable = null
            view.visibility = View.VISIBLE
            lastDecisionVisible = true
        } else if (activePkg != null) {
            view.visibility = View.GONE
            lastDecisionVisible = false
        }
    }

    private fun isTransientOrSystemUiPackage(pkg: String): Boolean {
        return pkg == "com.android.systemui" ||
                pkg == "android" ||
                pkg == "com.google.android.gms" ||
                pkg == "com.google.android.permissioncontroller" ||
                pkg == "com.android.permissioncontroller" ||
                pkg.contains("inputmethod", ignoreCase = true) ||
                pkg.contains("keyboard", ignoreCase = true) ||
                pkg.contains(".ime", ignoreCase = true) ||
                pkg == context.packageName
    }

    private fun evaluateVisibility(pkg: String?) {
        val view = overlayView ?: return
        val mode = repository.getFloatingButtonMode()
        if (mode == FloatingButtonMode.ALWAYS_VISIBLE) {
            pendingHideRunnable?.let { mainHandler.removeCallbacks(it) }
            pendingHideRunnable = null
            view.visibility = View.VISIBLE
            lastDecisionVisible = true
            return
        }

        val selectedPackages = repository.getSelectedPackages()
        val shouldBeVisible = pkg != null && selectedPackages.contains(pkg)

        if (shouldBeVisible) {
            // Cancel any pending hide immediately!
            pendingHideRunnable?.let { mainHandler.removeCallbacks(it) }
            pendingHideRunnable = null
            view.visibility = View.VISIBLE
            lastDecisionVisible = true
            if (isAttached && windowManager != null) {
                try {
                    windowManager.updateViewLayout(view, view.layoutParams)
                } catch (_: Exception) {}
            }
        } else {
            // Debounce hiding: wait 800ms to eliminate flickering during activity switches, menus, or dialogs
            if (lastDecisionVisible && pendingHideRunnable == null) {
                pendingHideRunnable = Runnable {
                    val currentPkg = getForegroundPackageName() ?: lastForegroundPackage
                    val stillTarget = currentPkg != null && selectedPackages.contains(currentPkg)
                    if (!stillTarget) {
                        view.visibility = View.GONE
                        lastDecisionVisible = false
                    }
                    pendingHideRunnable = null
                }
                mainHandler.postDelayed(pendingHideRunnable!!, 800)
            } else if (!lastDecisionVisible) {
                view.visibility = View.GONE
            }
        }
    }

    private fun buildFloatingPillView(): View {
        val density = context.resources.displayMetrics.density
        val appSettings = AppSettingsManager.getInstance(context).settings.value
        val sizeDp = appSettings.floatingButtonSizeDp
        val alphaPercent = appSettings.floatingButtonAlphaPercent
        val sizePx = (sizeDp * density).toInt()

        // Pixel-style Elegant hollow circular disc
        val container = FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(sizePx, sizePx)

            val circleRing = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.argb(35, 15, 23, 42)) // Subtle translucent dark glass center
                setStroke((2.2f * density).toInt(), Color.parseColor(appSettings.accentColorHex))
            }
            background = circleRing
            elevation = 10 * density
            alpha = alphaPercent / 100f
        }

        // Inner moon/power glyph
        val iconView = ImageView(context).apply {
            setImageResource(R.drawable.ic_qs_black_screen)
            setColorFilter(Color.parseColor(appSettings.accentColorHex))
            val iconSize = (sizePx * 0.48f).toInt()
            layoutParams = FrameLayout.LayoutParams(iconSize, iconSize).apply {
                gravity = Gravity.CENTER
            }
        }

        container.addView(iconView)
        return container
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchHandling(view: View, params: WindowManager.LayoutParams) {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isDragging = false
        var downTime = 0L

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    downTime = SystemClock.elapsedRealtime()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()

                    if (abs(dx) > 10 || abs(dy) > 10) {
                        isDragging = true
                        params.x = initialX + dx
                        params.y = initialY + dy
                        if (isAttached && windowManager != null) {
                            try {
                                windowManager.updateViewLayout(view, params)
                            } catch (_: Exception) {}
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val duration: Long = SystemClock.elapsedRealtime() - downTime
                    if (!isDragging) {
                        if (duration > 500L) {
                            // Long press: Open Castix Settings
                            openSettingsUi()
                        } else {
                            // Single tap: Immediately Turn Screen Black!
                            onFloatingButtonClicked()
                        }
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun onFloatingButtonClicked() {
        vibrateFeedback()
        // Immediately turn off the screen using pure AMOLED Black Screen overlay
        BlackScreenOverlayManager.getInstance(context).showBlackScreen()
    }

    private fun openSettingsUi() {
        vibrateFeedback()
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        context.startActivity(intent)
    }

    private fun vibrateFeedback() {
        try {
            val appSettings = AppSettingsManager.getInstance(context).settings.value
            if (!appSettings.floatingHapticFeedback) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(35)
            }
        } catch (_: Exception) {}
    }

    private fun startForegroundAppMonitoring() {
        stopForegroundAppMonitoring()
        appCheckRunnable = object : Runnable {
            override fun run() {
                checkAndUpdateVisibility()
                mainHandler.postDelayed(this, 1000) // Fast 1-second periodic fallback
            }
        }
        mainHandler.post(appCheckRunnable!!)
    }

    private fun stopForegroundAppMonitoring() {
        appCheckRunnable?.let { mainHandler.removeCallbacks(it) }
        appCheckRunnable = null
    }

    private fun checkAndUpdateVisibility() {
        val view = overlayView ?: return
        if (!isAttached) return

        val mode = repository.getFloatingButtonMode()
        if (mode == FloatingButtonMode.ALWAYS_VISIBLE) {
            view.visibility = View.VISIBLE
            return
        }

        // ONLY_SELECTED_APPS mode
        val currentForegroundPackage = getForegroundPackageName()
        if (currentForegroundPackage != null && !isTransientOrSystemUiPackage(currentForegroundPackage)) {
            evaluateVisibility(currentForegroundPackage)
        }
    }

    fun getForegroundPackageName(): String? {
        // 1. Accessibility Service is the primary, fastest, and most accurate detector
        CastixAccessibilityService.getActiveForegroundPackage()?.let { pkg ->
            if (!isTransientOrSystemUiPackage(pkg)) {
                return pkg
            }
        }

        // 2. UsageStatsManager Query for recent activity resume events
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return lastForegroundPackage

        val now = System.currentTimeMillis()
        val startTime = now - 1000 * 30 // Last 30 seconds

        try {
            val usageEvents = usageStatsManager.queryEvents(startTime, now)
            var lastEventPackage: String? = null
            val event = android.app.usage.UsageEvents.Event()

            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event)
                if (event.eventType == android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED) {
                    val p = event.packageName
                    if (p != null && !isTransientOrSystemUiPackage(p)) {
                        lastEventPackage = p
                    }
                }
            }
            if (lastEventPackage != null) {
                return lastEventPackage
            }
        } catch (_: Exception) {}

        // 3. Fallback: queryUsageStats to find top active app by lastTimeUsed
        try {
            val statsList = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_BEST,
                now - 1000 * 60 * 30, // last 30 mins
                now
            )
            val topApp = statsList
                ?.filter { !isTransientOrSystemUiPackage(it.packageName) }
                ?.maxByOrNull { it.lastTimeUsed }
                ?.packageName
            if (topApp != null) {
                return topApp
            }
        } catch (_: Exception) {}

        return lastForegroundPackage
    }
}
