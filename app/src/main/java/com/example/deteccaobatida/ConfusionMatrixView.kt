package com.example.deteccaobatida

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class ConfusionMatrixView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var matrix: Array<IntArray> = arrayOf()
    private val paint = Paint().apply {
        color = Color.BLACK
        textSize = 40f
        textAlign = Paint.Align.CENTER
    }

    fun setMatrix(matrix: Array<IntArray>) {
        this.matrix = matrix
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (matrix.isEmpty()) return

        val cellSize = width / matrix.size
        for (i in matrix.indices) {
            for (j in matrix[i].indices) {
                val x = j * cellSize + cellSize / 2
                val y = i * cellSize + cellSize / 2
                canvas.drawText(matrix[i][j].toString(), x.toFloat(), y.toFloat(), paint)
            }
        }
    }
}