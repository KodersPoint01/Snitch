package com.example.kotlintest.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat


class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {

            Log.d("TAG", "onReceive: BootReceiver ")
            Toast.makeText(context.applicationContext, "ACTION_BOOT_COMPLETED", Toast.LENGTH_SHORT)
                .show()
            Toast.makeText(context, "onReceive1212", Toast.LENGTH_SHORT).show()

            val serviceIntent = Intent(context.applicationContext, MyBackgroundService::class.java)
            ContextCompat.startForegroundService(context.applicationContext, serviceIntent)

        }

    }
}