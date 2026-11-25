package me.elhizazi.Castix.themes

import android.graphics.*
import android.graphics.drawable.Drawable
import android.widget.TextView
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import me.elhizazi.Castix.privilege.ui.ClockStyle

internal object NativeClock {
    fun render(view: TextView, style: ClockStyle, time: String, accent: Int, size: Float) {
        view.gravity = android.view.Gravity.CENTER
        view.setCompoundDrawables(null, null, null, null)
        view.textSize = size
        view.letterSpacing = 0f
        view.setLineSpacing(0f, 1f)
        view.setTextColor(Color.WHITE)
        view.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        when (style) {
            ClockStyle.DIGITAL_CLEAN -> {
                val parts = time.split(":", limit = 2)
                val value = parts.joinToString("\n")
                view.typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
                view.setLineSpacing(0f, .82f)
                view.letterSpacing = -.045f
                view.text = SpannableString(value).apply {
                    setSpan(ForegroundColorSpan(accent), 0, parts.first().length, 33)
                    if (parts.size == 2) setSpan(ForegroundColorSpan(0xffc4b5fd.toInt()), parts.first().length+1, length, 33)
                }
            }
            ClockStyle.MINIMAL_COMPACT -> { view.typeface = Typeface.create("sans-serif-thin", Typeface.NORMAL); view.letterSpacing = .07f; view.text = time }
            ClockStyle.ANALOG_MINIMAL -> {
                view.text = ""
                val px = (size * 2.8f * view.resources.displayMetrics.density).toInt()
                val dial = Dial(time, accent).apply { setBounds(0,0,px,px) }
                view.setCompoundDrawables(null,dial,null,null)
            }
            else -> view.text = time
        }
    }
    private class Dial(val time: String, val accent: Int): Drawable() {
        private val p = Paint(Paint.ANTI_ALIAS_FLAG)
        override fun draw(c: Canvas) {
            c.save(); c.translate(bounds.exactCenterX(), bounds.exactCenterY()); val r = bounds.width()*.43f
            p.color = accent; p.style = Paint.Style.STROKE; p.strokeWidth = r*.012f; c.drawCircle(0f,0f,r,p)
            p.color = 0xff555555.toInt(); c.drawCircle(0f,0f,r*.94f,p)
            p.style = Paint.Style.FILL
            for (i in 0..11) { c.save(); c.rotate(i*30f); p.color = if(i%3==0) Color.WHITE else 0xff888888.toInt(); c.drawRoundRect(-r*.018f,-r*.88f,r*.018f,-r*.76f,2f,2f,p); c.restore() }
            val h = time.substringBefore(":").toIntOrNull() ?: 0; val m = time.substringAfter(":").take(2).toIntOrNull() ?: 0
            p.color = Color.WHITE; p.textAlign = Paint.Align.CENTER; p.textSize = r*.17f
            for (i in listOf(12, 3, 6, 9)) { val angle = i * Math.PI / 6; c.drawText(i.toString(), (kotlin.math.sin(angle)*r*.64).toFloat(), (-kotlin.math.cos(angle)*r*.64).toFloat() - (p.ascent()+p.descent())/2, p) }
            p.strokeCap = Paint.Cap.ROUND; p.color = Color.WHITE; p.strokeWidth = r*.055f
            c.save(); c.rotate(h*30f+m*.5f); c.drawLine(0f,0f,0f,-r*.48f,p); c.restore()
            p.color = accent; p.strokeWidth = r*.035f; c.save(); c.rotate(m*6f); c.drawLine(0f,0f,0f,-r*.72f,p); c.restore(); c.drawCircle(0f,0f,r*.055f,p); c.restore()
        }
        override fun setAlpha(alpha: Int) { p.alpha = alpha }
        override fun setColorFilter(filter: ColorFilter?) { p.colorFilter = filter }
        @Deprecated("Deprecated in Java") override fun getOpacity() = PixelFormat.TRANSLUCENT
    }
}
