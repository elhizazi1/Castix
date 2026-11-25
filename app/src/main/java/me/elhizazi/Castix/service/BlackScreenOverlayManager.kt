package me.elhizazi.Castix.service

import me.elhizazi.Castix.privilege.ui.AppStrings
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.util.Log
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.elhizazi.Castix.R
import me.elhizazi.Castix.privilege.ui.AppSettingsManager
import me.elhizazi.Castix.privilege.ui.ClockStyle
import me.elhizazi.Castix.privilege.ui.LuxuryClockDrawable
import me.elhizazi.Castix.privilege.ui.luxuryClockStyles
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * High-performance AMOLED Pitch Black Screen Overlay (#000000).
 *
 * Clock and system stats are visible for five seconds after activation
 * or a single tap, then hidden on a pure black background.
 * Double tap anywhere unlocks immediately.
 */
class BlackScreenOverlayManager(private val context: Context) {

    companion object {
        private const val TAG = "BlackScreenManager"

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: BlackScreenOverlayManager? = null

        fun getInstance(context: Context): BlackScreenOverlayManager {
            return instance ?: synchronized(this) {
                instance ?: BlackScreenOverlayManager(context.applicationContext).also { instance = it }
            }
        }

        private val _isBlackScreenActive = MutableStateFlow(false)
        val isBlackScreenActive: StateFlow<Boolean> = _isBlackScreenActive.asStateFlow()
    }

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
    private var blackView: View? = null
    private var themeView: me.elhizazi.Castix.themes.ThemeWebView? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var clockUpdateRunnable: Runnable? = null
    private var dimRunnable: Runnable? = null
    private var infoContainerView: View? = null
    private var hintContainerView: View? = null

    // Preferences
    var doubleTapToWake: Boolean = true
    var showClock: Boolean = true
    var zeroBrightness: Boolean = true
    var clockStyle: ClockStyle = ClockStyle.DIGITAL_CLEAN
    var clockScalePercent: Int = 100
    var clockVerticalOffsetDp: Int = 0
    var clockSpacingPercent: Int = 100
    var showAppBadge: Boolean = true
    var showBattery: Boolean = true
    var showDate: Boolean = true
    var burnInProtection: Boolean = true
    var showPlaybackTimer: Boolean = true
    var customInscription: String = ""
    private var sessionStartTimeMillis: Long = 0L

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        val sp = context.getSharedPreferences("castix_black_screen_prefs", Context.MODE_PRIVATE)
        doubleTapToWake = sp.getBoolean("double_tap_to_wake", true)
        showClock = sp.getBoolean("show_clock", true)
        zeroBrightness = sp.getBoolean("zero_brightness", true)

        val globalSettings = AppSettingsManager.getInstance(context).settings.value
        clockStyle = globalSettings.clockStyle
        clockScalePercent = globalSettings.clockScalePercent
        clockVerticalOffsetDp = globalSettings.clockVerticalOffsetDp
        clockSpacingPercent = globalSettings.clockSpacingPercent
        showAppBadge = globalSettings.showAppBadgeOnBlackScreen
        showBattery = globalSettings.showBatteryOnBlackScreen
        showDate = globalSettings.showDateOnBlackScreen
        burnInProtection = globalSettings.burnInProtection
        showPlaybackTimer = globalSettings.showPlaybackTimer
        customInscription = globalSettings.customInscription
    }

    fun savePreferences(doubleTap: Boolean, clock: Boolean, zeroBright: Boolean) {
        doubleTapToWake = doubleTap
        showClock = clock
        zeroBrightness = zeroBright
        context.getSharedPreferences("castix_black_screen_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("double_tap_to_wake", doubleTap)
            .putBoolean("show_clock", clock)
            .putBoolean("zero_brightness", zeroBright)
            .apply()
    }

    @SuppressLint("ClickableViewAccessibility")
    fun showBlackScreen() {
        if (_isBlackScreenActive.value || windowManager == null) return
        if (!Settings.canDrawOverlays(context)) {
            Log.w(TAG, "Cannot show black screen: missing SYSTEM_ALERT_WINDOW permission")
            return
        }

        loadPreferences()
        sessionStartTimeMillis = System.currentTimeMillis()

        try {
            val flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED

            val layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                flags,
                PixelFormat.OPAQUE
            ).apply {
                gravity = Gravity.FILL
                x = 0
                y = 0
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
                screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
            }

            val container = buildBlackScreenView()
            setupGestureDetection(container)

            windowManager.addView(container, layoutParams)
            applyImmersiveFullscreen(container)
            blackView = container
            _isBlackScreenActive.value = true

            vibrateShort()
            scheduleAutoDim()
            Log.d(TAG, "Pure AMOLED Black Screen Overlay activated")
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying black screen", e)
            _isBlackScreenActive.value = false
            clockUpdateRunnable?.let { mainHandler.removeCallbacks(it) }
            clockUpdateRunnable = null
            dimRunnable?.let { mainHandler.removeCallbacks(it) }
            dimRunnable = null
            releaseTheme()
            blackView = null
            infoContainerView = null
            hintContainerView = null
        }
    }

    private fun releaseTheme() {
        themeView?.let { view -> (view.parent as? android.view.ViewGroup)?.removeView(view); view.destroy() }
        themeView = null
    }

    fun hideBlackScreen() {
        clockUpdateRunnable?.let { mainHandler.removeCallbacks(it) }
        clockUpdateRunnable = null
        dimRunnable?.let { mainHandler.removeCallbacks(it) }
        dimRunnable = null

        if (_isBlackScreenActive.value && blackView != null && windowManager != null) {
            try {
                windowManager.removeView(blackView)
            } catch (e: Exception) {
                Log.e(TAG, "Error removing black screen view", e)
            }
        }
        releaseTheme()
        blackView = null
        infoContainerView = null
        hintContainerView = null
        _isBlackScreenActive.value = false
        vibrateShort()
        Log.d(TAG, "AMOLED Black Screen dismissed")
    }

    fun toggleBlackScreen() {
        if (_isBlackScreenActive.value) {
            hideBlackScreen()
        } else {
            showBlackScreen()
        }
    }

    fun reassertImmersiveOnWake() {
        mainHandler.post {
            blackView?.let {
                applyImmersiveFullscreen(it)
            }
        }
    }

    private fun setAmbientVisible(visible: Boolean) {
        val root = blackView ?: return
        infoContainerView?.visibility = if (visible) View.VISIBLE else View.INVISIBLE
        hintContainerView?.visibility = if (visible) View.VISIBLE else View.INVISIBLE
        themeView?.setAmbientActive(visible)
        val params = root.layoutParams as? WindowManager.LayoutParams ?: return
        val brightness = if (!visible && zeroBrightness) 0.005f else WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        if (params.screenBrightness != brightness) {
            params.screenBrightness = brightness
            runCatching { windowManager?.updateViewLayout(root, params) }
                .onFailure { Log.w(TAG, "Unable to update overlay brightness", it) }
        }
    }

    private fun scheduleAutoDim() {
        dimRunnable?.let { mainHandler.removeCallbacks(it) }
        setAmbientVisible(true)
        dimRunnable = null
        val settings = AppSettingsManager.getInstance(context).settings.value
        if (!settings.ambientAutoDim) return
        val task = Runnable {
            dimRunnable = null
            setAmbientVisible(false)
        }
        dimRunnable = task
        mainHandler.postDelayed(task, settings.ambientDurationSeconds * 1000L)
    }

    private fun wakeInfoToProminent() = scheduleAutoDim()

    private fun applyImmersiveFullscreen(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.windowInsetsController?.let { controller ->
                controller.hide(
                    WindowInsets.Type.statusBars() or
                    WindowInsets.Type.navigationBars() or
                    WindowInsets.Type.displayCutout() or
                    WindowInsets.Type.systemBars()
                )
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
        @Suppress("DEPRECATION")
        view.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LOW_PROFILE
        )
    }

    private fun buildBlackScreenView(): FrameLayout {
        val root = object : FrameLayout(context) {
            override fun onInterceptTouchEvent(ev: MotionEvent): Boolean = true
        }.apply {
            setBackgroundColor(Color.BLACK)
            isClickable = true
            isFocusable = true
            fitsSystemWindows = false
            addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    applyImmersiveFullscreen(v)
                }
                override fun onViewDetachedFromWindow(v: View) {}
            })
            @Suppress("DEPRECATION")
            setOnSystemUiVisibilityChangeListener { visibility ->
                if ((visibility and View.SYSTEM_UI_FLAG_FULLSCREEN) == 0 ||
                    (visibility and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
                    applyImmersiveFullscreen(this)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                setOnApplyWindowInsetsListener { v, insets ->
                    applyImmersiveFullscreen(v)
                    insets
                }
            }
        }

        if (showClock) {
            val globalSettings = AppSettingsManager.getInstance(context).settings.value
            val custom = me.elhizazi.Castix.themes.ThemeStore(context).find(globalSettings.customThemeId)
            if (custom != null) {
                val web = me.elhizazi.Castix.themes.ThemeWebView(context, custom) {
                    mainHandler.post {
                        AppSettingsManager.getInstance(context).selectCustomTheme(null)
                        if (_isBlackScreenActive.value) { hideBlackScreen(); showBlackScreen() }
                    }
                }
                web.sessionStart = sessionStartTimeMillis
                web.foreground = FloatingOverlayManager.getActiveForegroundPackage()
                themeView = web
                infoContainerView = web
                root.addView(web, FrameLayout.LayoutParams(-1, -1))
                if (burnInProtection) {
                    clockUpdateRunnable = object : Runnable {
                        override fun run() {
                            if (!_isBlackScreenActive.value) return
                            web.translationX = (-8..8).random().toFloat()
                            web.translationY = (-8..8).random().toFloat()
                            mainHandler.postDelayed(this, 60000)
                        }
                    }
                    mainHandler.postDelayed(clockUpdateRunnable!!, 60000)
                }
            } else {
            val accentColor = Color.parseColor(globalSettings.accentColorHex)

            val infoLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                alpha = 1f
            }
            infoContainerView = infoLayout

            val timeView = TextView(context).apply {
                if (clockStyle in luxuryClockStyles) {
                    textSize = 42f
                    setTextColor(Color.WHITE)
                    typeface = android.graphics.Typeface.create("sans-serif-light", android.graphics.Typeface.NORMAL)
                    setPadding(58, 68, 58, 68)
                    background = LuxuryClockDrawable(clockStyle)
                }
                when (clockStyle) {
                    ClockStyle.NEON_ORBIT, ClockStyle.GOLDEN_ARCH, ClockStyle.SAKURA_MOON, ClockStyle.CHRONOGRAPH, ClockStyle.CYBER_HUD, ClockStyle.LUMINOUS_BUTTERFLY, ClockStyle.EARTHRISE, ClockStyle.MOONLIT_MOUNTAINS, ClockStyle.NEON_DRAGON, ClockStyle.ROYAL_MANDALA, ClockStyle.AUDIO_AURA, ClockStyle.CRYSTAL_LOTUS -> Unit
                    ClockStyle.LARGE_GLOW -> {
                        textSize = 54f
                        setTextColor(accentColor)
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }
                    ClockStyle.MINIMAL_COMPACT -> {
                        textSize = 24f
                        setTextColor(Color.WHITE)
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }
                    ClockStyle.ANALOG_MINIMAL -> {
                        textSize = 34f
                        setTextColor(Color.WHITE)
                        typeface = android.graphics.Typeface.MONOSPACE
                    }
                    ClockStyle.RETRO_FLIP -> {
                        textSize = 36f
                        setTextColor(accentColor)
                        typeface = android.graphics.Typeface.MONOSPACE
                    }
                    ClockStyle.TEXT_WORD -> {
                        textSize = 22f
                        setTextColor(Color.WHITE)
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }
                    ClockStyle.PIXEL_AIRY -> {
                        textSize = 46f
                        setTextColor(Color.WHITE)
                        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
                        letterSpacing = 0.055f
                    }
                    ClockStyle.MATERIAL_EXPRESSIVE -> {
                        textSize = 40f
                        setTextColor(Color.WHITE)
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                        letterSpacing = -0.025f
                    }
                    ClockStyle.LARGE_CENTER -> {
                        textSize = 62f
                        setTextColor(Color.WHITE)
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }
                    ClockStyle.STACKED -> {
                        textSize = 48f
                        setTextColor(Color.WHITE)
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }
                    ClockStyle.MONO_LINE -> {
                        textSize = 25f
                        setTextColor(Color.WHITE)
                        typeface = android.graphics.Typeface.MONOSPACE
                    }
                    ClockStyle.DATA_DASHBOARD -> {
                        textSize = 42f
                        setTextColor(Color.WHITE)
                        typeface = android.graphics.Typeface.MONOSPACE
                    }
                    else -> {
                        textSize = 40f
                        setTextColor(Color.WHITE)
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }
                }
                gravity = Gravity.CENTER
            }

            val dateView = TextView(context).apply {
                textSize = when (clockStyle) {
                    ClockStyle.DATA_DASHBOARD -> 11f
                    ClockStyle.STACKED -> 12f
                    ClockStyle.MINIMAL_COMPACT -> 11f
                    else -> 13f
                }
                setTextColor(Color.LTGRAY)
                gravity = Gravity.CENTER
                setPadding(0, 4, 0, 0)
                visibility = if (showDate) View.VISIBLE else View.GONE
            }

            val batteryView = TextView(context).apply {
                textSize = 12f
                setTextColor(Color.parseColor("#4ADE80"))
                gravity = Gravity.CENTER
                setPadding(0, 4, 0, 0)
                visibility = if (showBattery) View.VISIBLE else View.GONE
            }

            // Playback session duration timer
            val playbackTimerView = TextView(context).apply {
                textSize = 11f
                setTextColor(Color.parseColor("#94A3B8"))
                gravity = Gravity.CENTER
                setPadding(0, 4, 0, 0)
                visibility = if (showPlaybackTimer) View.VISIBLE else View.GONE
            }

            // Active Background App Badge (e.g. YouTube / Spotify / Facebook playing)
            val appBadgeView = TextView(context).apply {
                textSize = 12f
                setTextColor(accentColor)
                gravity = Gravity.CENTER
                setPadding(0, 6, 0, 0)
                visibility = if (showAppBadge) View.VISIBLE else View.GONE
            }

            // Optional custom user inscription/slogan
            val inscriptionView = TextView(context).apply {
                textSize = 11f
                setTextColor(Color.GRAY)
                gravity = Gravity.CENTER
                setPadding(0, 8, 0, 0)
                alpha = 0.7f
                visibility = if (customInscription.isNotBlank()) View.VISIBLE else View.GONE
                text = customInscription
            }

            infoLayout.addView(timeView)
            infoLayout.addView(dateView)
            infoLayout.addView(batteryView)
            infoLayout.addView(playbackTimerView)
            infoLayout.addView(appBadgeView)
            infoLayout.addView(inscriptionView)
            val notificationView = TextView(context).apply { textSize = 12f; setTextColor(Color.LTGRAY); gravity = Gravity.CENTER }
            infoLayout.addView(notificationView)

            val infoParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }
            root.addView(infoLayout, infoParams)

            // Professional clock tuning is shared by every template and persisted globally.
            val scale = (clockScalePercent / 100f).coerceIn(0.75f, 1.4f)
            infoLayout.scaleX = scale
            infoLayout.scaleY = scale
            infoLayout.translationY = clockVerticalOffsetDp * context.resources.displayMetrics.density
            val spacingPx = (4f * (clockSpacingPercent / 100f)).toInt().coerceIn(2, 8)
            dateView.setPadding(0, spacingPx, 0, 0)
            batteryView.setPadding(0, spacingPx, 0, 0)
            playbackTimerView.setPadding(0, spacingPx, 0, 0)
            appBadgeView.setPadding(0, (spacingPx + 2).coerceAtMost(10), 0, 0)

            // Update clock and foreground app info
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val dateFormat = SimpleDateFormat("EEE, d MMM", Locale.getDefault())

            fun updateClock() {
                val now = Date()
                val count = if (globalSettings.showNotifications) me.elhizazi.Castix.themes.ThemeNotifications.count else null
                notificationView.visibility = if (count != null && count > 0) View.VISIBLE else View.GONE
                notificationView.text = if (count != null) "◉ $count" else ""
                when (clockStyle) {
                    ClockStyle.ANALOG_MINIMAL -> {
                        timeView.text = "● ${timeFormat.format(now)} ◯"
                    }
                    ClockStyle.RETRO_FLIP -> {
                        val hours = SimpleDateFormat("HH", Locale.getDefault()).format(now)
                        val mins = SimpleDateFormat("mm", Locale.getDefault()).format(now)
                        timeView.text = "[ $hours : $mins ]"
                    }
                    ClockStyle.PIXEL_AIRY -> {
                        timeView.text = timeFormat.format(now)
                    }
                    ClockStyle.MATERIAL_EXPRESSIVE -> {
                        timeView.text = timeFormat.format(now)
                    }
                    ClockStyle.TEXT_WORD -> {
                        val cal = java.util.Calendar.getInstance()
                        val h = cal.get(java.util.Calendar.HOUR_OF_DAY)
                        val m = cal.get(java.util.Calendar.MINUTE)
                        val lang = AppSettingsManager.getInstance(context).settings.value.language
                        if (lang == "ar") {
                            timeView.text = AppStrings.getForContext(context, "black_screen_clock_time_ar", h, m)
                        } else {
                            timeView.text = AppStrings.getForContext(context, "black_screen_clock_time_en", h, m)
                        }
                    }
                    ClockStyle.STACKED -> {
                        val hours = SimpleDateFormat("HH", Locale.getDefault()).format(now)
                        val mins = SimpleDateFormat("mm", Locale.getDefault()).format(now)
                        timeView.text = "$hours\n$mins"
                    }
                    ClockStyle.MONO_LINE -> {
                        val hours = SimpleDateFormat("HH", Locale.getDefault()).format(now)
                        val mins = SimpleDateFormat("mm", Locale.getDefault()).format(now)
                        timeView.text = "$hours  •  $mins"
                    }
                    ClockStyle.DATA_DASHBOARD -> {
                        timeView.text = timeFormat.format(now)
                    }
                    else -> {
                        timeView.text = timeFormat.format(now)
                    }
                }

                if (clockStyle in listOf(ClockStyle.DIGITAL_CLEAN, ClockStyle.MINIMAL_COMPACT, ClockStyle.ANALOG_MINIMAL)) {
                    me.elhizazi.Castix.themes.NativeClock.render(timeView, clockStyle, timeFormat.format(now), accentColor, 40f)
                }
                if (showDate) {
                    dateView.text = dateFormat.format(now)
                    dateView.visibility = View.VISIBLE
                } else {
                    dateView.visibility = View.GONE
                }

                if (showBattery) {
                    val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
                    val level = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
                    val stableStr = AppStrings.getForContext(context, "battery_stable")
                    batteryView.text = if (level >= 0) "$level% • $stableStr" else ""
                    batteryView.visibility = View.VISIBLE
                } else {
                    batteryView.visibility = View.GONE
                }

                if (showPlaybackTimer && sessionStartTimeMillis > 0L) {
                    val elapsedMinutes = ((System.currentTimeMillis() - sessionStartTimeMillis) / 60000L).coerceAtLeast(0)
                    val lang = AppSettingsManager.getInstance(context).settings.value.language
                    playbackTimerView.text = AppStrings.getForContext(
                        context,
                        "black_screen_session_timer",
                        elapsedMinutes
                    )
                    playbackTimerView.visibility = View.VISIBLE
                } else {
                    playbackTimerView.visibility = View.GONE
                }

                if (showAppBadge) {
                    try {
                        val activePkg = FloatingOverlayManager.getActiveForegroundPackage()
                        if (!activePkg.isNullOrEmpty() && activePkg != context.packageName) {
                            val pm = context.packageManager
                            val appInfo = pm.getApplicationInfo(activePkg, 0)
                            val appLabel = pm.getApplicationLabel(appInfo).toString()
                            appBadgeView.text = AppStrings.getForContext(context, "black_screen_app_playing", appLabel)
                        } else {
                            appBadgeView.text = AppStrings.getForContext(context, "black_screen_continuous_play")
                        }
                    } catch (_: Exception) {
                        appBadgeView.text = AppStrings.getForContext(context, "black_screen_continuous_play")
                    }
                    appBadgeView.visibility = View.VISIBLE
                } else {
                    appBadgeView.visibility = View.GONE
                }

                if (customInscription.isNotBlank()) {
                    inscriptionView.text = customInscription
                    inscriptionView.visibility = View.VISIBLE
                } else {
                    inscriptionView.visibility = View.GONE
                }

                // OLED Burn-in Protection: Subtly shift clock pixels periodically
                if (burnInProtection && infoContainerView != null) {
                    val randomShiftX = ((-12..12).random()).toFloat()
                    val randomShiftY = ((-12..12).random()).toFloat()
                    infoContainerView?.animate()?.translationX(randomShiftX)?.translationY(randomShiftY)?.setDuration(1200)?.start()
                }
            }

            updateClock()

            clockUpdateRunnable = object : Runnable {
                override fun run() {
                    if (_isBlackScreenActive.value) {
                        updateClock()
                        mainHandler.postDelayed(this, 60000)
                    }
                }
            }
            mainHandler.postDelayed(clockUpdateRunnable!!, 60000)
        }

        }

        // Bottom hint: Double tap to wake
        val hintLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            alpha = 0.25f
        }

        val hintText = TextView(context).apply {
            text = if (doubleTapToWake) {
                AppStrings.getForContext(context, "black_screen_unlock_hint")
            } else {
                AppStrings.getForContext(context, "black_screen_long_press_unlock")
            }
            textSize = 11f
            setTextColor(Color.GRAY)
        }

        hintLayout.addView(hintText)

        val hintParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            bottomMargin = 90
        }
        root.addView(hintLayout, hintParams)
        hintContainerView = hintLayout

        return root
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupGestureDetection(view: View) {
        val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean = true

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                // Single tap reveals the design for another five seconds.
                wakeInfoToProminent()
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                if (doubleTapToWake) {
                    hideBlackScreen()
                    return true
                }
                return false
            }

            override fun onLongPress(e: MotionEvent) {
                if (!doubleTapToWake) {
                    hideBlackScreen()
                }
            }
        })

        view.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    private fun vibrateShort() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                v?.vibrate(40)
            }
        } catch (_: Exception) {}
    }
}
