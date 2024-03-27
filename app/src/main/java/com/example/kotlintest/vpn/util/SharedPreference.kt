package com.example.kotlintest.vpn.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.example.kotlintest.R
import com.example.kotlintest.vpn.Server


class SharedPreference {
    private val APP_PREFS_NAME = "CakeVPNPreference"
    private var mPreference: SharedPreferences? = null
    private var mPrefEditor: SharedPreferences.Editor? = null
    private var context: Context? = null
    private val SERVER_COUNTRY = "server_country"
    private val SERVER_FLAG = "server_flag"
    private val SERVER_OVPN = "server_ovpn"
    private val SERVER_OVPN_USER = "server_ovpn_user"
    private val SERVER_OVPN_PASSWORD = "server_ovpn_password"
    @SuppressLint("NotConstructor")
    constructor(context: Context)  {
        mPreference =
            context.getSharedPreferences(APP_PREFS_NAME, Context.MODE_PRIVATE)
        mPrefEditor = mPreference!!.edit()
        this.context = context
    }
    fun saveServer(server: Server) {
        mPrefEditor!!.putString(SERVER_COUNTRY, server.country)
        server.flagUrl?.let { mPrefEditor!!.putInt(SERVER_FLAG, it) }
        mPrefEditor!!.putString(SERVER_OVPN, server.ovpn)
        mPrefEditor!!.putString(SERVER_OVPN_USER, server.ovpnUserName)
        mPrefEditor!!.putString(SERVER_OVPN_PASSWORD, server.ovpnUserPassword)
        mPrefEditor!!.commit()
    }
    fun getServer(): Server? {
        return Server(
            mPreference!!.getString(SERVER_COUNTRY, "Japan"),
            mPreference!!.getInt(SERVER_FLAG, R.drawable.japan_flag),
            mPreference!!.getString(SERVER_OVPN, "japan.ovpn"),
            mPreference!!.getString(SERVER_OVPN_USER, "I8JWpRJ6TU9f6tQZ"),
            mPreference!!.getString(SERVER_OVPN_PASSWORD, "FbEcP6cg9NchqdNSEsj8sHDeUG8T0xXM")
        )
    }
    fun clear()
    {
        val settings = context!!.getSharedPreferences(APP_PREFS_NAME, Context.MODE_PRIVATE)
        settings.edit().remove(SERVER_COUNTRY).apply()
        settings.edit().remove(SERVER_FLAG).apply()
        settings.edit().remove(SERVER_OVPN).apply()
        settings.edit().remove(SERVER_OVPN_USER).apply()
        settings.edit().remove(SERVER_OVPN_PASSWORD).apply()
    }
    fun getImgURL(resourceId: Int): String? {

        // Use BuildConfig.APPLICATION_ID instead of R.class.getPackage().getName() if both are not same
        return Uri.parse("android.resource://" + R::class.java.getPackage().name + "/" + resourceId)
            .toString()
    }
}