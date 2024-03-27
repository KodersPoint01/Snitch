package com.example.kotlintest.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.kotlintest.MainActivity
import com.example.kotlintest.R

class SplashActivity : AppCompatActivity() {

    private var handler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        handler = Handler(Looper.getMainLooper())
        handler!!.postDelayed(Runnable {
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler!!.removeCallbacksAndMessages(null)
    }
}