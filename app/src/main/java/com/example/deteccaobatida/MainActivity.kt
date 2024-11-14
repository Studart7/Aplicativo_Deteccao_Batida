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
import android.os.CountDownTimer
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.widget.GridLayout
import android.widget.TextView

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var previousTruePositiveCount = 0
    private var isPopupActive = false // Controle para verificar se um popup já está ativo
    private var countDownTimer: CountDownTimer? = null // Variável para o cronômetro

    private lateinit var tvAccelerometer: TextView
    private lateinit var tvGyroscope: TextView
    private lateinit var confusionMatrixView: ConfusionMatrixView
    private lateinit var tvMetrics: TextView
    private lateinit var gridConfusionMatrix: GridLayout

    private val LIMITE_DESACELERACAO = 90.0f // Defina o valor conforme necessário
    private val LIMITE_GIRO = 70.0f // Defina o valor conforme necessário
    private val numClasses = 2 // Número de classes
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

        // Inicia o SensorService
        val serviceIntent = Intent(this, SensorService::class.java)
        startService(serviceIntent)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        var acelerometroDetectou = false
        var giroscopioDetectou = false

        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    val x = it.values[0]
                    val y = it.values[1]
                    val z = it.values[2]
                    val magnitude = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()

                    // Verifica se a magnitude do acelerômetro excede o limite
                    if (magnitude > LIMITE_DESACELERACAO) {
                        acelerometroDetectou = true
                    }

                    tvAccelerometer.text = "Acelerômetro: \nX: $x m/s² \nY: $y m/s² \nZ: $z m/s²"
                }
                Sensor.TYPE_GYROSCOPE -> {
                    val x = it.values[0]
                    val y = it.values[1]
                    val z = it.values[2]
                    val magnitude = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()

                    // Verifica se a magnitude do giroscópio excede o limite
                    if (magnitude > LIMITE_GIRO) {
                        giroscopioDetectou = true
                    }

                    tvGyroscope.text = "Giroscópio: \nX: $x rad/s \nY: $y rad/s \nZ: $z rad/s"
                }
            }

            // Atualiza a matriz de confusão com base nos resultados dos sensores
            if (acelerometroDetectou && giroscopioDetectou) {
                // True Positive (houve uma batida e foi detectada)
                updateConfusionMatrix(1, 0)
            } else if (acelerometroDetectou || giroscopioDetectou) {
                // False Positive (detecção de batida falsa)
                updateConfusionMatrix(0, 0)
            } else {
                // True Negative (não houve batida e não foi detectada)
                updateConfusionMatrix(1, 1)
            }
        }
    }

    private fun updateConfusionMatrix(trueLabel: Int, predictedLabel: Int) {
        if (trueLabel in 0 until numClasses && predictedLabel in 0 until numClasses) {
            val matrixBeforeUpdate = confusionMatrix.getMatrix().map { it.clone() } // Clona o estado atual da matriz
            confusionMatrix.addPrediction(trueLabel, predictedLabel)
            val matrixAfterUpdate = confusionMatrix.getMatrix()
    
            // Atualiza a interface da matriz
            updateGridConfusionMatrix(matrixAfterUpdate)
            displayMetrics()
    
            // Verifica se a célula de True Positive (matriz[1][1]) foi incrementada
            if (trueLabel == 0 && predictedLabel == 0) {
                val currentTruePositiveCount = matrixAfterUpdate[0][0]
                if (currentTruePositiveCount > previousTruePositiveCount && !isPopupActive) {
                    showCrashDialog() // Exibe o popup quando o valor é incrementado
                    previousTruePositiveCount = currentTruePositiveCount // Atualiza a contagem anterior
                }
            }
        } else {
            println("Índices inválidos: trueLabel=$trueLabel, predictedLabel=$predictedLabel")
        }
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
        if (!isPopupActive) {
            isPopupActive = true // Marca que um popup está ativo
    
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Atenção!")
            builder.setMessage("Uma batida foi detectada! Ação será tomada em 15 segundos.")
    
            // Adiciona o botão Confirmar batida
            builder.setPositiveButton("Confirmar batida") { dialog, _ ->
                dialog.dismiss()
                isPopupActive = false // Reseta a variável ao fechar o popup
                countDownTimer?.cancel() // Cancela o cronômetro se o usuário confirmar
                // Atualiza a matriz de confusão para True Positive (1, 1)
                updateConfusionMatrix(1, 1)
            }
    
            // Adiciona o botão Alarme falso
            builder.setNegativeButton("Alarme falso") { dialog, _ ->
                dialog.dismiss()
                isPopupActive = false // Reseta a variável ao fechar o popup
                countDownTimer?.cancel() // Cancela o cronômetro se o usuário confirmar
                // Não atualiza a matriz de confusão para False Positive (0, 0)
            }
    
            val dialog = builder.create()
    
            // Impede que o dialog seja fechado ao clicar fora dele
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
    
            // Exibe o popup
            dialog.show()
    
            // Inicia o cronômetro de 15 segundos
            startCountDownTimer(dialog)
        }
    }
    
    private fun startCountDownTimer(dialog: AlertDialog) {
        countDownTimer = object : CountDownTimer(15000, 1000) { // 15 segundos, decrementando a cada 1 segundo
            override fun onTick(millisUntilFinished: Long) {
                // Atualiza a mensagem do popup com o tempo restante
                dialog.setMessage("Uma batida foi detectada! Ação será tomada em ${millisUntilFinished / 1000} segundos.")
            }
    
            override fun onFinish() {
                // Quando o cronômetro terminar, atualiza a matriz de confusão para True Positive (1, 1)
                updateConfusionMatrix(1, 1)
                isPopupActive = false // Marca que o popup pode ser fechado
                dialog.dismiss() // Fecha o popup quando o cronômetro terminar
            }
        }
    
        countDownTimer?.start() // Inicia o cronômetro
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Não é necessário lidar com isso para este exemplo
    }

    override fun onStart() {
        super.onStart()
        val serviceIntent = Intent(this, SensorService::class.java)
        startService(serviceIntent) // Inicia o serviço em primeiro plano
        LocalBroadcastManager.getInstance(this).registerReceiver(
            crashReceiver,
            IntentFilter("com.example.deteccaobatida.CRASH_DETECTED")
        )
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