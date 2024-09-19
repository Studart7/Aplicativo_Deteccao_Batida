package com.example.deteccaobatida

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null

    private lateinit var tvAccelerometer: TextView
    private lateinit var tvGyroscope: TextView
    private lateinit var tvCrashAlert: TextView

    private val accelerometerValues = mutableListOf<FloatArray>()
    private val gyroscopeValues = mutableListOf<FloatArray>()

    private val threshold = 0.1f // Define o limiar de exibição normal
    private val impactThreshold = 80.0f // Aumente o limiar para detectar uma batida

    private var alertDialog: AlertDialog? = null // Armazena o diálogo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvAccelerometer = findViewById(R.id.tvAccelerometer)
        tvGyroscope = findViewById(R.id.tvGyroscope)
        tvCrashAlert = findViewById(R.id.tvCrashAlert)

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
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    val smoothedValues = applyMovingAverage(it.values)
                    val x = smoothedValues[0]
                    val y = smoothedValues[1]
                    val z = smoothedValues[2]

                    // Calcula a magnitude total da aceleração
                    val totalAcceleration = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()

                    if (Math.abs(x) > threshold || Math.abs(y) > threshold || Math.abs(z) > threshold) {
                        tvAccelerometer.text = "Acelerômetro: \nX: $x m/s² \nY: $y m/s² \nZ: $z m/s²"
                    }

                    // Verifica se o total de aceleração excede o limiar de batida
                    if (totalAcceleration > impactThreshold) {
                        showCrashDetectedDialog()
                    } else {
                        // Reseta a mensagem de alerta quando a aceleração está abaixo do limiar
                        tvCrashAlert.text = "Detectando..."
                    }
                }
                Sensor.TYPE_GYROSCOPE -> {
                    val smoothedValues = applyMovingAverage(it.values)
                    val x = smoothedValues[0]
                    val y = smoothedValues[1]
                    val z = smoothedValues[2]
                    if (Math.abs(x) > threshold || Math.abs(y) > threshold || Math.abs(z) > threshold) {
                        tvGyroscope.text = "Giroscópio: \nX: $x rad/s \nY: $y rad/s \nZ: $z rad/s"
                    }
                }
            }
        }
    }

    private fun showCrashDetectedDialog() {
        // Atualiza o TextView para mostrar que uma batida foi detectada
        tvCrashAlert.text = "Batida detectada!"

        // Verifica se o diálogo já está sendo exibido
        if (alertDialog == null || !alertDialog!!.isShowing) {
            alertDialog = AlertDialog.Builder(this)
                .setTitle("Batida Detectada!")
                .setMessage("Uma batida foi detectada. Verifique o veículo.")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss() // Fecha o diálogo
                    alertDialog = null // Reseta o diálogo
                }
                .setOnDismissListener {
                    alertDialog = null // Reseta o diálogo quando fechado
                }
                .create()

            alertDialog?.show()
        }
    }

    private fun applyMovingAverage(values: FloatArray): FloatArray {
        val size = 5
        if (accelerometerValues.size >= size) {
            accelerometerValues.removeAt(0)
        }
        accelerometerValues.add(values.copyOf())
        val avg = FloatArray(values.size)
        for (i in values.indices) {
            avg[i] = accelerometerValues.map { it[i] }.average().toFloat()
        }
        return avg
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Não é necessário lidar com isso para este exemplo
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
