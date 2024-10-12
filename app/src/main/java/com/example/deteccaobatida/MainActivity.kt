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
import android.widget.TextView

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null

    private lateinit var tvAccelerometer: TextView
    private lateinit var tvGyroscope: TextView
    private lateinit var confusionMatrixView: ConfusionMatrixView // Change to ConfusionMatrixView

    private val threshold = 70.0f // Define the threshold as needed
    private val numClasses = 3
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
        confusionMatrixView = findViewById(R.id.confusionMatrixView) // Ensure this is ConfusionMatrixView

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
        confusionMatrixView.setMatrix(confusionMatrix.getMatrix()) // This should now work
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