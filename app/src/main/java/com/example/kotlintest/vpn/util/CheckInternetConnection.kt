package com.example.kotlintest.vpn.util

import android.content.Context
import android.net.ConnectivityManager

class CheckInternetConnection {
    fun netCheck(context: Context): Boolean {
        val cm =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nInfo = cm.activeNetworkInfo
        return nInfo != null && nInfo.isConnectedOrConnecting
    }
}