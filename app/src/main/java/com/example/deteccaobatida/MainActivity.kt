package com.example.deteccaobatida

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.deteccaobatida.services.SensorService
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.widget.GridLayout
import android.widget.TextView

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null

    private lateinit var tvAccelerometer: TextView
    private lateinit var tvGyroscope: TextView
    private lateinit var confusionMatrixView: ConfusionMatrixView
    private lateinit var tvMetrics: TextView
    private lateinit var gridConfusionMatrix: GridLayout

    private val threshold = 70.0f // Define the threshold as needed
    private val numClasses = 2
    private val confusionMatrix = ConfusionMatrix(numClasses)

    private val crashReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            showCrashDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvAccelerometer = findViewById(R.id.tvAccelerometer)
        tvGyroscope = findViewById(R.id.tvGyroscope)
        confusionMatrixView = findViewById(R.id.confusionMatrixView)
        tvMetrics = findViewById(R.id.tvMetrics)
        gridConfusionMatrix = findViewById(R.id.gridConfusionMatrix)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        if (accelerometer == null) {
            tvAccelerometer.text = "Acelerômetro não disponível"
        }
        if (gyroscope == null) {
            tvGyroscope.text = "Giroscópio não disponível"
        } else {
            accelerometer?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
            gyroscope?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        }

        // Start the SensorService
        val serviceIntent = Intent(this, SensorService::class.java)
        startService(serviceIntent)

        // Register the crash receiver
        registerReceiver(crashReceiver, IntentFilter("com.example.deteccaobatida.CRASH_DETECTED"))
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    val x = it.values[0]
                    val y = it.values[1]
                    val z = it.values[2]
                    if (Math.abs(x) > threshold || Math.abs(y) > threshold || Math.abs(z) > threshold) {
                        // Envia um broadcast quando uma batida é detectada
                        val intent = Intent("com.example.deteccaobatida.CAR_CRASH")
                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                    }
                    tvAccelerometer.text = "Acelerômetro: \nX: $x m/s² \nY: $y m/s² \nZ: $z m/s²"
                    updateConfusionMatrix(0, 1) // Example: Update confusion matrix with accelerometer data
                }
                Sensor.TYPE_GYROSCOPE -> {
                    val x = it.values[0]
                    val y = it.values[1]
                    val z = it.values[2]
                    tvGyroscope.text = "Giroscópio: \nX: $x rad/s \nY: $y rad/s \nZ: $z rad/s"
                    updateConfusionMatrix(1, 2) // Example: Update confusion matrix with gyroscope data
                }
            }
        }
    }

    private fun updateConfusionMatrix(trueLabel: Int, predictedLabel: Int) {
        confusionMatrix.addPrediction(trueLabel, predictedLabel)
        updateGridConfusionMatrix(confusionMatrix.getMatrix()) // Update the GridLayout
        displayMetrics() // Display the updated metrics
    }

    private fun updateGridConfusionMatrix(matrix: Array<IntArray>) {
        val cellIds = arrayOf(
            intArrayOf(R.id.cell00, R.id.cell01),
            intArrayOf(R.id.cell10, R.id.cell11)
        )

        for (i in matrix.indices) {
            for (j in matrix[i].indices) {
                val cell = findViewById<TextView>(cellIds[i][j])
                cell.text = matrix[i][j].toString()
            }
        }
    }

    private fun displayMetrics() {
        val metrics = StringBuilder()
        for (i in 0 until numClasses) {
            val recall = confusionMatrix.getRecall(i)
            val precision = confusionMatrix.getPrecision(i)
            val f1Score = confusionMatrix.getF1Score(i)
            metrics.append("Class $i:\n")
            metrics.append("Recall: $recall\n")
            metrics.append("Precision: $precision\n")
            metrics.append("F1 Score: $f1Score\n\n")
        }
        val accuracy = confusionMatrix.getAccuracy()
        metrics.append("Accuracy: $accuracy\n")
        tvMetrics.text = metrics.toString()
    }

    private fun showCrashDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Atenção!")
        builder.setMessage("Uma batida foi detectada!")
        builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Não é necessário lidar com isso para este exemplo
    }

    override fun onStart() {
        super.onStart()
        val serviceIntent = Intent(this, SensorService::class.java)
        startService(serviceIntent) // Inicia o serviço em primeiro plano
        LocalBroadcastManager.getInstance(this).registerReceiver(crashReceiver, IntentFilter("com.example.deteccaobatida.CAR_CRASH"))
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(crashReceiver)
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        gyroscope?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
}