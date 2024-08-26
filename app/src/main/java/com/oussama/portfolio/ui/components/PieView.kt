package com.oussama.portfolio.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.oussama.portfolio.R

class PieView : View {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 2f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private lateinit var colors: List<Int>
    private val angles = floatArrayOf(120f, 120f, 120f)
    private val rectF = RectF()
    private var borderColor: Int = 0

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        val attributes =
            context.obtainStyledAttributes(attrs, R.styleable.PieView, defStyleAttr, 0)
        val colorPrimary = attributes.getColor(R.styleable.PieView_pieColorPrimary, Color.GREEN)
        val colorSecondary = attributes.getColor(R.styleable.PieView_pieColorSecondary, Color.RED)
        val colorTertiary = attributes.getColor(R.styleable.PieView_pieColorTertiary, Color.BLUE)
        borderColor = attributes.getColor(R.styleable.PieView_pieBorderColor, Color.BLACK)
        colors = listOf(colorSecondary, colorPrimary, colorTertiary)
        strokePaint.color = borderColor
        attributes.recycle()
    }

    private fun setColors(colors: List<Int>) {
        this.colors = colors
        invalidate()
    }

    fun setColors(colorPrimary: Int, colorSecondary: Int, colorTertiary: Int) {
        setColors(listOf(colorSecondary, colorPrimary, colorTertiary))
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2f
        val centerY = height / 2f
        paint.color = borderColor
        canvas.drawCircle(centerX, centerY, Math.min(centerX, centerY), paint)
        val radius = Math.min(centerX, centerY) - 1f
        rectF.left = centerX - radius
        rectF.top = centerY - radius
        rectF.right = centerX + radius
        rectF.bottom = centerY + radius
        var startAngle = 0f
        for (i in colors.indices) {
            paint.color = colors[i]
            canvas.drawArc(rectF, startAngle, angles[i], true, paint)
            canvas.drawArc(rectF, startAngle, angles[i], true, strokePaint)
            startAngle += angles[i]
        }
    }
}