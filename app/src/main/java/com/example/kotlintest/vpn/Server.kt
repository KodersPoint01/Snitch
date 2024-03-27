package com.example.kotlintest.vpn

import java.io.Serializable

class Server : Serializable {
    var country: String? = null
    var flagUrl: Int? = null
    var ovpn: String? = null
    var ovpnUserName: String? = null
    var ovpnUserPassword: String? = null

    constructor() {}
    constructor(country: String?, flagUrl: Int?, ovpn: String?) {
        this.country = country
        this.flagUrl = flagUrl
        this.ovpn = ovpn
    }

    constructor(
        country: String?,
        flagUrl: Int?,
        ovpn: String?,
        ovpnUserName: String?,
        ovpnUserPassword: String?
    ) {
        this.country = country
        this.flagUrl = flagUrl
        this.ovpn = ovpn
        this.ovpnUserName = ovpnUserName
        this.ovpnUserPassword = ovpnUserPassword
    }
}