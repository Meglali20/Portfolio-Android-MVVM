package com.oussama.portfolio.ui.components.drawables

import android.graphics.Canvas
import androidx.compose.ui.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.graphics.PixelFormat
import androidx.compose.ui.graphics.toArgb

class ScanLinesDrawable : Drawable() {
    private val horizontalPaint = Paint().apply {
        color = Color(0xA8B8B8AE).toArgb() //#13111100
        strokeWidth = 0.1f
        style = Paint.Style.STROKE
    }
    /*private val verticalPaint = Paint().apply {
        color = Color.parseColor("#ff00000f")
    }*/

    override fun draw(canvas: Canvas) {
        val bounds = bounds
        // Draw horizontal scanlines
        for (y in 0 until bounds.height() step 5) {
            //canvas.drawRect(bounds.left.toFloat(), y.toFloat(), bounds.right.toFloat(), (y + 0.1).toFloat(), horizontalPaint)
            canvas.drawLine(bounds.left.toFloat(), y.toFloat(),bounds.right.toFloat(), y.toFloat(), horizontalPaint)
        }
        // Draw vertical scanlines
       /* for (x in 3 until bounds.width() step 3) {
            canvas.drawRect(x.toFloat(), bounds.top.toFloat(), (x + 2).toFloat(), bounds.bottom.toFloat(), verticalPaint)
        }*/
    }

    override fun setAlpha(alpha: Int) {
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

}
