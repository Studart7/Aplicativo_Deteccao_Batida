package com.example.deteccaobatida

class ConfusionMatrix(private val numClasses: Int) {
    private val matrix: Array<IntArray> = Array(numClasses) { IntArray(numClasses) }

    fun addPrediction(trueLabel: Int, predictedLabel: Int) {
        matrix[trueLabel][predictedLabel]++
    }

    fun getMatrix(): Array<IntArray> {
        return matrix
    }

    fun resetMatrix() {
        for (i in matrix.indices) {
            for (j in matrix[i].indices) {
                matrix[i][j] = 0
            }
        }
    }

    override fun toString(): String {
        val builder = StringBuilder()
        for (i in matrix.indices) {
            for (j in matrix[i].indices) {
                builder.append(matrix[i][j]).append(" ")
            }
            builder.append("\n")
        }
        return builder.toString()
    }
}