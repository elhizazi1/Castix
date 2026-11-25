package me.elhizazi.Castix.themes

import android.content.Context
import android.graphics.Color
import android.webkit.*
import android.view.MotionEvent
import java.io.ByteArrayInputStream

internal class ThemeWebView(context: Context, private val theme: CastixTheme, private val preview: Boolean = false, private val failure: () -> Unit = {}) : WebView(context) {
    private var disposed = false
    private var ambientActive = true
    private val runtimeScript by lazy { context.assets.open("castix/theme-runtime.js").bufferedReader().use { it.readText() } }
    var sessionStart: Long = 0L
    var foreground: String? = null
    private val ticker = object : Runnable {
        override fun run() {
            if (disposed || !ambientActive || !isAttachedToWindow) return
            publishData()
            postDelayed(this, 1000 * 5L)
        }
    }
    fun publishData() {
        if (disposed || !ambientActive) return
        val data = ThemeData.snapshot(context, sessionStart, foreground, preview)
        evaluateJavascript("window.CastixData=$data;window.dispatchEvent(new CustomEvent('castix:data',{detail:window.CastixData}));" + runtimeScript, null)
    }
    fun setAmbientActive(active: Boolean) {
        if (disposed || ambientActive == active) return
        ambientActive = active
        removeCallbacks(ticker)
        if (active) { onResume(); if (isAttachedToWindow) post(ticker) }
        else onPause()
    }
    override fun destroy() { if (!disposed) { disposed = true; removeCallbacks(ticker); super.destroy() } }
    private val host = "castix-theme.invalid"
    init {
        setBackgroundColor(Color.BLACK)
        isFocusable = false
        settings.apply {
            javaScriptEnabled = true
            allowFileAccess = false
            allowContentAccess = false
            blockNetworkLoads = true
            domStorageEnabled = false
            javaScriptCanOpenWindowsAutomatically = false
            setSupportMultipleWindows(false)
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
        }
        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) { publishData() }
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest) = true
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse {
                val uri = request.url
                return runCatching {
                    require(uri.scheme == "https" && uri.host == host && request.method == "GET")
                    val file = ThemeStore.safeFile(theme.folder, requireNotNull(uri.path).removePrefix("/"))
                    val mime = when (file.extension.lowercase()) {
                        "html" -> "text/html"; "css" -> "text/css"; "js", "mjs" -> "application/javascript"; "svg" -> "image/svg+xml"; "json" -> "application/json"; "woff2" -> "font/woff2"; "woff" -> "font/woff"; "ttf" -> "font/ttf"; else -> android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension.lowercase()) ?: "application/octet-stream"
                    }
                    WebResourceResponse(mime, if (mime.startsWith("text/") || mime == "application/javascript") "UTF-8" else null, 200, "OK", mapOf("Content-Security-Policy" to "default-src 'none'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self'; media-src 'none'; connect-src 'none'; frame-src 'none'; object-src 'none'; base-uri 'none'; form-action 'none'"), file.inputStream())
                }.getOrElse { WebResourceResponse("text/plain", "UTF-8", 403, "Blocked", emptyMap(), ByteArrayInputStream(byteArrayOf())) }
            }
            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) { if (request.isForMainFrame) failure() }
            override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, errorResponse: WebResourceResponse) { if (request.isForMainFrame) failure() }
            override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean { failure(); (parent as? android.view.ViewGroup)?.removeView(this@ThemeWebView); destroy(); return true }
        }
        loadUrl("https://$host/" + theme.entry.split('/').joinToString("/") { android.net.Uri.encode(it) })
    }
    override fun onTouchEvent(event: MotionEvent): Boolean = false
    override fun onDetachedFromWindow() { removeCallbacks(ticker); super.onDetachedFromWindow(); onPause() }
    override fun onAttachedToWindow() { super.onAttachedToWindow(); removeCallbacks(ticker); if (ambientActive) { onResume(); post(ticker) } }
}
