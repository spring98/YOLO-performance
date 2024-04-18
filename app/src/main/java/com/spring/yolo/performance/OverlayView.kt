package com.spring.yolo.performance

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class OverlayView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
        private val paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }

    private val detectedObjects = mutableListOf<RectF>()

    fun drawRectangles(rectangles: List<RectF>) {
        detectedObjects.clear()
        detectedObjects.addAll(rectangles)

        // View 를 다시 그리도록 요청
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (rect in detectedObjects) {
            canvas.drawRect(rect, paint)
        }
    }
}
