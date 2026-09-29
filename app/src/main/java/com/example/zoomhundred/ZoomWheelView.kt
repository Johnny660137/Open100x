package com.example.zoomhundred

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

class ZoomWheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var onZoomRatioChanged: ((Float) -> Unit)? = null

    private val minZoom = 1.0f
    private val maxZoom = 100.0f
    private var currentProgress = 0f

    private var lastX = 0f
    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 3f
    }
    private val centerIndicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFCC00")
        strokeWidth = 6f
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                parent.requestDisallowInterceptTouchEvent(true)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.x - lastX
                lastX = event.x

                val sensitivity = 0.0015f
                currentProgress = (currentProgress - deltaX * sensitivity).coerceIn(0f, 1f)

                val zoomRatio = minZoom * (maxZoom / minZoom).pow(currentProgress)
                onZoomRatioChanged?.invoke(zoomRatio)

                invalidate()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent.requestDisallowInterceptTouchEvent(false)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f

        canvas.drawLine(cx, 10f, cx, height - 10f, centerIndicatorPaint)

        val tickSpacing = 24f
        val offset = (currentProgress * 2000f) % tickSpacing

        for (x in -12..12) {
            val tickX = cx + (x * tickSpacing) - offset
            val distanceFromCenter = abs(tickX - cx) / (width / 2f)
            if (distanceFromCenter <= 1f) {
                tickPaint.alpha = ((1f - distanceFromCenter) * 255).toInt()
                val tickHeight = if (x % 5 == 0) height * 0.65f else height * 0.35f
                val top = cy - (tickHeight / 2f)
                val bottom = cy + (tickHeight / 2f)
                canvas.drawLine(tickX, top, tickX, bottom, tickPaint)
            }
        }
    }
}
