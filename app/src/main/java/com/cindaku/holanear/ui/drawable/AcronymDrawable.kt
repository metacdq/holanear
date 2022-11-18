package com.cindaku.holanear.ui.drawable
import android.graphics.*
import android.graphics.drawable.Drawable
import kotlin.math.min

class AcronymDrawable(private val acronym: String, private val sizeFactor: Float,private val rounded: Boolean=true) : Drawable() {

    private val paintBackground = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG)
    private val colors: List<Int> = listOf(
        Color.parseColor("#21ac94"),
        Color.parseColor("#29ae5c"),
        Color.parseColor("#a6c706"),
        Color.parseColor("#ff7308"),
        Color.parseColor("#ff7307"),
        Color.parseColor("#f0592f"),
        Color.parseColor("#cd4b7d"),
        Color.parseColor("#9d5cb3"),
        Color.parseColor("#675cb3")
    )
    private val boundsRect = RectF()

    init {
        val backgroundColor = colors[Math.abs(acronym.hashCode() % colors.size)]
        setColors(backgroundColor)
    }

    override fun draw(canvas: Canvas) {
        boundsRect.set(bounds)

        paintText.textSize = bounds.height() / sizeFactor
        if(rounded){
            val radius = min(boundsRect.width(), boundsRect.height()) / 2
            canvas.drawCircle(boundsRect.centerX(), boundsRect.centerY(), radius, paintBackground)
        }else{
            canvas.drawRect(boundsRect,paintBackground)
        }
        val yTextPosition = boundsRect.centerY() - paintText.ascent() / 2 - paintText.descent() / 2
        canvas.drawText(acronym, boundsRect.centerX(), yTextPosition, paintText)
    }

    override fun setAlpha(alpha: Int) {
        paintText.alpha = alpha
        paintBackground.alpha = alpha
    }

    override fun getOpacity() = PixelFormat.UNKNOWN

    override fun setColorFilter(colorFilter: ColorFilter?) {
        /* Not used */
    }

    private fun setColors(backgroundColor: Int, textColor: Int = 0xFFFFFFFF.toInt()) {
        paintBackground.color = backgroundColor

        with(paintText) {
            color = textColor
            textAlign = Paint.Align.CENTER
        }
    }
}
