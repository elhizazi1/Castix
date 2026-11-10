package me.elhizazi.Castix.privilege.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Same decorative renderer in settings previews and the actual AMOLED overlay. */
val luxuryClockStyles = setOf(
    ClockStyle.NEON_ORBIT, ClockStyle.GOLDEN_ARCH, ClockStyle.SAKURA_MOON, ClockStyle.CHRONOGRAPH, ClockStyle.CYBER_HUD, ClockStyle.LUMINOUS_BUTTERFLY, ClockStyle.EARTHRISE, ClockStyle.MOONLIT_MOUNTAINS, ClockStyle.NEON_DRAGON, ClockStyle.ROYAL_MANDALA, ClockStyle.AUDIO_AURA, ClockStyle.CRYSTAL_LOTUS
)

class LuxuryClockDrawable(private val style: ClockStyle) : Drawable() {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    override fun draw(canvas: Canvas) {
        val b = bounds
        val w = b.width().toFloat()
        val h = b.height().toFloat()
        if (w <= 0f || h <= 0f) return
        val checkpoint = canvas.save()
        canvas.translate(b.left.toFloat(), b.top.toFloat())
        val cx = w / 2f
        val cy = h / 2f
        val r = minOf(w * .43f, h * .43f)
        val palette = when (style) {
            ClockStyle.GOLDEN_ARCH, ClockStyle.ROYAL_MANDALA -> intArrayOf(0xFFFFCB73.toInt(), 0xFF7A4D20.toInt())
            ClockStyle.SAKURA_MOON -> intArrayOf(0xFFFF8FCB.toInt(), 0xFF9D367D.toInt())
            ClockStyle.LUMINOUS_BUTTERFLY, ClockStyle.AUDIO_AURA -> intArrayOf(0xFF45D9FF.toInt(), 0xFFA15CFF.toInt())
            ClockStyle.EARTHRISE -> intArrayOf(0xFF58BFFF.toInt(), 0xFFFFC86E.toInt())
            ClockStyle.MOONLIT_MOUNTAINS -> intArrayOf(0xFF77E7DF.toInt(), 0xFF205C7E.toInt())
            else -> intArrayOf(0xFF48F5F0.toInt(), 0xFF197B99.toInt())
        }
        fun stroke(color: Int, width: Float = 2f, alpha: Int = 210) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = width
            paint.color = color
            paint.alpha = alpha
            paint.setShadowLayer(width * 2.5f, 0f, 0f, color)
        }
        fun circle(x: Float, y: Float, rad: Float) = canvas.drawCircle(x, y, rad, paint)
        fun line(x: Float, y: Float, x2: Float, y2: Float) = canvas.drawLine(x, y, x2, y2, paint)
        when (style) {
            ClockStyle.NEON_ORBIT, ClockStyle.EARTHRISE, ClockStyle.CRYSTAL_LOTUS -> {
                stroke(palette[0], 2.2f)
                circle(cx, cy, r)
                stroke(palette[1], 1f, 150)
                circle(cx, cy, r * .89f)
                for (i in 0 until 28) {
                    val a = i * 2 * PI / 28
                    val x = cx + cos(a).toFloat() * r
                    val y = cy + sin(a).toFloat() * r
                    circle(x, y, if (i % 7 == 0) 2.5f else 1f)
                }
            }
            ClockStyle.GOLDEN_ARCH, ClockStyle.ROYAL_MANDALA -> {
                for (layer in 0..1) {
                    stroke(palette[layer], if (layer == 0) 2.3f else 1.1f)
                    val rr = r * (1f - layer * .13f)
                    val path = Path()
                    for (i in 0..96) {
                        val a = i * 2 * PI / 96
                        val petal = 1f + .10f * cos(a * if (style == ClockStyle.GOLDEN_ARCH) 8 else 12).toFloat()
                        val x = cx + cos(a).toFloat() * rr * petal
                        val y = cy + sin(a).toFloat() * rr * petal
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    path.close()
                    canvas.drawPath(path, paint)
                }
            }
            ClockStyle.SAKURA_MOON, ClockStyle.LUMINOUS_BUTTERFLY -> {
                for (i in 0 until 10) {
                    val a = i * 2 * PI / 10
                    val x = cx + cos(a).toFloat() * r * .90f
                    val y = cy + sin(a).toFloat() * r * .90f
                    stroke(if (i % 2 == 0) palette[0] else palette[1], 1.5f)
                    for (j in 0 until 5) {
                        val t = j * 2 * PI / 5
                        circle(x + cos(t).toFloat() * 8f, y + sin(t).toFloat() * 8f, 5f)
                    }
                }
            }
            ClockStyle.CHRONOGRAPH, ClockStyle.CYBER_HUD -> {
                stroke(palette[0], 2f)
                circle(cx, cy, r)
                for (i in 0 until 60) {
                    val a = i * 2 * PI / 60 - PI / 2
                    val inner = r * if (i % 5 == 0) .84f else .93f
                    line(cx + cos(a).toFloat() * inner, cy + sin(a).toFloat() * inner,
                         cx + cos(a).toFloat() * r, cy + sin(a).toFloat() * r)
                }
            }
            ClockStyle.MOONLIT_MOUNTAINS, ClockStyle.NEON_DRAGON -> {
                stroke(palette[0], 2f)
                val path = Path()
                for (i in 0..90) {
                    val x = w * i / 90f
                    val y = cy + r * .65f + sin(i * .18).toFloat() * r * .17f + sin(i * .41).toFloat() * r * .09f
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                canvas.drawPath(path, paint)
                stroke(palette[1], 1.5f)
                circle(cx, cy, r * .92f)
            }
            ClockStyle.AUDIO_AURA -> {
                for (i in 0 until 35) {
                    val x = w * (.12f + i * .76f / 34f)
                    val length = (8 + 23 * kotlin.math.abs(sin(i * .65))).toFloat()
                    stroke(if (i % 3 == 0) palette[1] else palette[0], 2f)
                    line(x, cy + r * .75f - length / 2, x, cy + r * .75f + length / 2)
                }
                stroke(palette[1], 1.5f)
                circle(cx, cy, r * .94f)
            }
            else -> Unit
        }
        paint.clearShadowLayer()
        canvas.restoreToCount(checkpoint)
    }
    override fun setAlpha(alpha: Int) { paint.alpha = alpha; invalidateSelf() }
    override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) { paint.colorFilter = colorFilter; invalidateSelf() }
    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
