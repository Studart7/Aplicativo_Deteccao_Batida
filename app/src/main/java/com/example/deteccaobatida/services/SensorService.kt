package com.example.deteccaobatida.services

import android.app.Service
import android.content.Intent
import android.os.IBinder

class SensorService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Your service logic here
        return START_STICKY
    }
}