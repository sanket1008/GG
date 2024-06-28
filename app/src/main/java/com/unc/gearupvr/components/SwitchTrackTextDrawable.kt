package com.unc.gearupvr.components

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.NonNull
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.unc.gearupvr.R


class SwitchTrackTextDrawable(@NonNull val context: Context, @StringRes val leftTextId: Int, @StringRes val rightTextId: Int) :
    Drawable() {

    private var _leftText: String = context.getString(leftTextId)
    private var _rightText: String = context.getString(rightTextId)
    private val _textPaint: Paint
    private val _backgroundPaint: Paint

    init {
        _textPaint = createTextPaint()
        _backgroundPaint = createBackgroundPaint()
    }

    private fun createTextPaint(): Paint {
        val textPaint = Paint()

        textPaint.color = ContextCompat.getColor(context, R.color.colorAccent)
        textPaint.isAntiAlias = true
        textPaint.style = Paint.Style.FILL
        textPaint.textAlign = Paint.Align.CENTER
        val fontSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            15F,
            context.resources.displayMetrics
        )
        textPaint.textSize = fontSize
        textPaint.typeface = ResourcesCompat.getFont(context, R.font.roboto_regular)

        return textPaint
    }

    private fun createBackgroundPaint(): Paint {
        val paint = Paint()

        paint.color = ContextCompat.getColor(
            context, android.R.color.white
        )
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL

        return paint
    }

    override fun draw(canvas: Canvas) {

        //Draw background layer
        val bgRect = RectF(
            0F,
            0F,
            bounds.width().toFloat(),
            bounds.height().toFloat()
        )

        val cornersRadius = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            35F,
            context.resources.displayMetrics
        )
        canvas.drawRoundRect(bgRect, cornersRadius, cornersRadius, _backgroundPaint)

        //Draw text
        val textBounds = Rect()
        _textPaint.getTextBounds(_rightText, 0, _rightText.length, textBounds)

        // The baseline for the text: centered, including the height of the text itself
        val heightBaseline: Float = (bounds.height() / 2 + textBounds.height() / 2).toFloat()

        // This is one quarter of the full width, to measure the centers of the texts
        val widthQuarter: Float = (bounds.width() / 4).toFloat()
        //drawText(@NonNull String text, int start, int end, float x, float y, @NonNull Paint paint)
        canvas.drawText(
            _leftText, 0, _leftText.length,
            widthQuarter, heightBaseline,
            _textPaint
        )
        canvas.drawText(
            _rightText, 0, _rightText.length,
            widthQuarter * 3, heightBaseline,
            _textPaint
        )
    }

    override fun setAlpha(p0: Int) {
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun setColorFilter(p0: ColorFilter?) {
    }
}