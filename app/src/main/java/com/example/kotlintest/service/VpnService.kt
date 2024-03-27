package com.example.kotlintest.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.kotlintest.R
import de.blinkt.openvpn.OpenVpnApi
import java.io.BufferedReader
import java.io.InputStreamReader


class VpnService : Service() {


    companion object {
        private var vpnStart = false
        const val CHANNEL_ID = "ALARM_SERVICE_CHANNEL"
    }


    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()


    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "EncodeVpn service is running in the background",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d("Service", "onStartCommand: AlarmService")

        startVpn()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("EncodeVpn")
            .setContentText("EncodeVpn service is running in the background")
            .setSmallIcon(R.drawable.ic_notification)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .build()
        startForeground(1, notification)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun startVpn() {
        if (!vpnStart) {
            try {
                // Load the OpenVPN configuration file
                val confStream = assets.open("kor.ovpn")
                val confReader = BufferedReader(InputStreamReader(confStream))
                var config = ""
                var line: String?

                while (confReader.readLine().also { line = it } != null) {
                    config += "$line\n"
                }

                confReader.close()

                // Start the VPN connection
                OpenVpnApi.startVpn(
                    this,
                    config,
                    "Jappan5",
                    "vpn",
                    "vpn"
                )

                vpnStart = true
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }
        }
    }


}