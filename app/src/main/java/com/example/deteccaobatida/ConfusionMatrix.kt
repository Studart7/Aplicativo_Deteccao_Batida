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

    // Verifica quantos casos positivos foram classificados corretamente
    fun getRecall(classIndex: Int): Double {
        val truePositive = matrix[classIndex][classIndex]
        var falseNegative = 0
        for (i in matrix.indices) {
            if (i != classIndex) {
                falseNegative += matrix[classIndex][i]
            }
        }
        return if (truePositive + falseNegative == 0) {
            0.0
        } else {
            truePositive.toDouble() / (truePositive + falseNegative)
        }
    }

    // verifica quantos casos classificados como positivos são realmente positivos
    fun getPrecision(classIndex: Int): Double {
        val truePositive = matrix[classIndex][classIndex]
        var falsePositive = 0
        for (i in matrix.indices) {
            if (i != classIndex) {
                falsePositive += matrix[i][classIndex]
            }
        }
        return if (truePositive + falsePositive == 0) {
            0.0
        } else {
            truePositive.toDouble() / (truePositive + falsePositive)
        }
    }

    // Calcula o F1 Score usando 
    fun getF1Score(classIndex: Int): Double {
        val precision = getPrecision(classIndex)
        val recall = getRecall(classIndex)
        return if (precision + recall == 0.0) {
            0.0
        } else {
            2 * (precision * recall) / (precision + recall)
        }
    }

    // calcula a acurácia total da matriz de confusão
    fun getAccuracy(): Float {
        val truePositives = (0 until numClasses).sumBy { matrix[it][it] }
        val totalSamples = matrix.sumBy { it.sum() }
        return if (totalSamples == 0) 0f else truePositives.toFloat() / totalSamples
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