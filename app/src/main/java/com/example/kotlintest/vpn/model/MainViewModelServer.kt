package com.example.kotlintest.vpn.model

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kotlintest.R
import com.example.kotlintest.vpn.Server


class MainViewModelServer : ViewModel() {
    private val listServer = MutableLiveData<ArrayList<Server>>()

    private fun isSystemPackage(pkgInfo: PackageInfo): Boolean {
        return pkgInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
    }

    fun populateServerList(): MutableLiveData<ArrayList<Server>> {
        val servers = java.util.ArrayList<Server>()
        servers.add(
            Server(
                "Japan",
                R.drawable.japan_flag,
                "japan.ovpn",
                "I8JWpRJ6TU9f6tQZ",
                "FbEcP6cg9NchqdNSEsj8sHDeUG8T0xXM"
            )
        )
        servers.add(
            Server(
                "Korea",
                R.drawable.korea_flag,
                "korea.ovpn",
                "vpn",
                "vpn"
            )
        )
        servers.add(
            Server(
                "USA",
                R.drawable.usa_flag,
                "us.ovpn",
                "I8JWpRJ6TU9f6tQZ",
                "FbEcP6cg9NchqdNSEsj8sHDeUG8T0xXM"
            )
        )
        servers.add(
            Server(
                "USA 2",
                R.drawable.usa_flag,
                "us.ovpn",
                "I8JWpRJ6TU9f6tQZ",
                "FbEcP6cg9NchqdNSEsj8sHDeUG8T0xXM"
            )
        )
        servers.add(
            Server(
                "Japan2",
                R.drawable.japan_flag,
                "japan.ovpn",
                "I8JWpRJ6TU9f6tQZ",
                "FbEcP6cg9NchqdNSEsj8sHDeUG8T0xXM"
            )
        )
/*
        servers.add(
            Server(
                "Japan3",
                R.drawable.japan_flag,
                "jpan.ovpn",
                "vpn",
                "vpn"
            )
        )
        servers.add(
            Server(
                "Japan4",
                R.drawable.japan_flag,
                "japnapermanent.ovpn",
                "vpn",
                "vpn"
            )
        )*/

       /* servers.add(
            Server(
                "Korea2",
                R.drawable.korea_flag,
                "kor.ovpn",
                "vpn",
                "vpn"
            )
        )*/
/*
        servers.add(
            Server(
                "Jappan5",
                R.drawable.japan_flag,
                "jopan.ovpn",
                "vpn",
                "vpn"
            )
        )*/
        listServer.value = servers
        return listServer
    }
}