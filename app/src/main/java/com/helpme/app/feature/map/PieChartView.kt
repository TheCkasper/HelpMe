package com.helpme.app.feature.map

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

data class PieEntry(
    val label: String,
    val count: Int,
    val color: Int
)

class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var entries: List<PieEntry> = emptyList()
    private var totalSum = 0

    private val slicePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val innerHolePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    private val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 20f
        color = Color.parseColor("#E0E0E0")
    }

    private val rectF = RectF()

    fun setData(newEntries: List<PieEntry>) {
        this.entries = newEntries.filter { it.count > 0 }
        this.totalSum = entries.sumOf { it.count }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        if (width <= 0 || height <= 0) return

        val size = min(width, height)
        val padding = 16f
        val radius = (size - padding * 2) / 2f
        val centerX = width / 2f
        val centerY = height / 2f

        rectF.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )

        if (totalSum <= 0 || entries.isEmpty()) {
            // Draw empty placeholder ring
            canvas.drawCircle(centerX, centerY, radius - 10f, emptyPaint)
            return
        }

        var startAngle = -90f

        for (entry in entries) {
            val sweepAngle = (entry.count.toFloat() / totalSum.toFloat()) * 360f
            slicePaint.color = entry.color

            canvas.drawArc(rectF, startAngle, sweepAngle, true, slicePaint)
            startAngle += sweepAngle
        }

        // Draw center hole to turn pie into a clean modern Donut chart
        val holeRadius = radius * 0.55f
        canvas.drawCircle(centerX, centerY, holeRadius, innerHolePaint)
    }
}
