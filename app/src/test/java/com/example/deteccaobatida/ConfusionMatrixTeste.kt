package com.example.deteccaobatida

import org.junit.Test
import org.junit.Assert.*

class ModelEvaluationTest {
    @Test
    fun testConfusionMatrix() {
        val numClasses = 3
        val confusionMatrix = ConfusionMatrix(numClasses)

        // Simulate some predictions
        confusionMatrix.addPrediction(0, 0)
        confusionMatrix.addPrediction(0, 1)
        confusionMatrix.addPrediction(1, 1)
        confusionMatrix.addPrediction(2, 2)
        confusionMatrix.addPrediction(2, 0)

        // Print the confusion matrix
        println(confusionMatrix)

        // Add assertions as needed
        val matrix = confusionMatrix.getMatrix()
        assertEquals(1, matrix[0][0])
        assertEquals(1, matrix[0][1])
        assertEquals(1, matrix[1][1])
        assertEquals(1, matrix[2][2])
        assertEquals(1, matrix[2][0])
    }
}