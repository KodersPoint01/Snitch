package com.example.kotlintest.vpn;

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.VpnService
import android.os.Bundle
import android.os.Handler
import android.os.RemoteException
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.example.kotlintest.R
import com.example.kotlintest.databinding.ActivityVpnConnectivityBinding
import com.example.kotlintest.vpn.Interfaces.ChangeServer
import com.example.kotlintest.vpn.model.MainViewModelServer
import com.example.kotlintest.vpn.model.ServerModel
import com.example.kotlintest.vpn.util.CheckInternetConnection
import com.example.kotlintest.vpn.util.SharedPreference
import com.google.android.material.card.MaterialCardView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.blinkt.openvpn.OpenVpnApi
import de.blinkt.openvpn.core.OpenVPNService
import de.blinkt.openvpn.core.OpenVPNThread
import de.blinkt.openvpn.core.VpnStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class VpnConnectivity : AppCompatActivity(), ChangeServer {
    private var dialog: Dialog? = null

    //  private var serverLists: ArrayList<Server>? = null
    private var server: Server? = null
    private var connection: CheckInternetConnection? = null
    private var changeServer: ChangeServer? = null
    private var flag: Boolean = false
    var vpnStart = false
    private var preference: SharedPreference? = null
    private var binding: ActivityVpnConnectivityBinding? = null
    var hostName: String? = ""


    var serverLists: ArrayList<Server> = ArrayList()


    // var db: AppDatabase? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVpnConnectivityBinding.inflate(layoutInflater)
        val view: View = binding!!.root
        setContentView(view)
//        db = Room.databaseBuilder(
//            applicationContext,
//            AppDatabase::class.java, "MyDataBase"
//        ).allowMainThreadQueries().build()
        val model: MainViewModelServer by viewModels()
        model.populateServerList().observe(this) {
            serverLists = it
            Log.d("TAG", "onCreate all serverLists: $serverLists")
            server = serverLists.get(2)
            Log.d("prepareVpn", "onCreate:server $server ")
            Log.d("prepareVpn", "onCreate:server ${serverLists.get(2).ovpn} ")
        }

        init()
        //serverLists = ArrayList()
        //serverLists = populateServerList()
        isServiceRunning()
        try {
            VpnStatus.initLogCache(this.cacheDir)
        } catch (e: Exception) {
        }
        recieveIntent()
        Handler()
            .postDelayed(Runnable {
                if (getInternetStatus()) {
                    try {
                        flag = true
                        binding!!.txtFindingServer.visibility = View.GONE
                        binding!!.layoutCurrentSever.visibility = View.VISIBLE
                        server = preference!!.getServer()
                        if (!this.isDestroyed) {
                            Glide.with(this)
                                .load(server!!.flagUrl)
                                .into(binding!!.imgServerFlag)
                        }
                        binding!!.txtServerName.text = server!!.country
                        Log.d("TAG", "onCreate: ${server!!.country}")
                    } catch (e: Exception) {
                    }
                } else {
                    binding!!.txtFindingServer.text = "Please Check Your Internet Connection"
                }

            }, 2000)
    }

    private fun recieveIntent() {
        try {
            server = intent.getSerializableExtra("currentServer") as Server?
            Log.d("TAG", "onItemClick recieve: $" + server)
            if (server != null) {
                /* if (vpnStart) {
                     confirmDisconnect()
                 } else {*/
                vpnStart = false
                prepareVpn()
                Glide.with(this)
                    .load(server!!.flagUrl)
                    .into(binding!!.imgServerFlag)
                binding!!.txtServerName.text = server!!.country

            }
        } catch (e: NullPointerException) {

        }
    }


    private fun init() {
        binding!!.selectServer.setOnClickListener {
            startActivity(Intent(this, AvailableServerList::class.java))
//            finish()
            // showServerDialog()
        }
        preference = SharedPreference(this)
        binding!!.btnConnection.setOnClickListener(View.OnClickListener {
            if (flag) {
                if (vpnStart) {
                    confirmDisconnect()
                } else {
                    prepareVpn()
                }
            } else {
                Toast.makeText(this, "Please wait  for finding the best server", Toast.LENGTH_SHORT)
                    .show()
            }


        })
        binding!!.imgQrBackBtn.setOnClickListener {
            onBackPressed()
        }
        // Update current selected server icon
//        updateCurrentServerIcon(server!!.flagUrl)
        connection = CheckInternetConnection()
    }

    fun confirmDisconnect() {
        dialog = Dialog(this)
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog!!.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        dialog!!.setContentView(R.layout.disconnnectdailog)
        val materialNo: MaterialCardView = dialog!!.findViewById(R.id.materialNo)
        val materialYes: MaterialCardView = dialog!!.findViewById(R.id.materialYes)
        materialNo.setOnClickListener {
            dialog!!.dismiss()
        }
        materialYes.setOnClickListener {
            stopVpn()
            dialog!!.dismiss()

        }
        dialog!!.show()
    }

    private fun prepareVpn() {
        if (!vpnStart) {
//            Glide.with(this)
//                .asGif()
//                .load(R.drawable.animationvpncon)
//                .into(binding!!.appCompatImageView!!)
            if (getInternetStatus()) {
                // Checking permission for network monitor
                val intent = VpnService.prepare(this)
                if (intent != null) {
                    startActivityForResult(intent, 1)
                } else {
                    startVpn()
                    startVpnFirebase()
                }
                //have already permission
                // Update confection status
                status("connecting")
            } else {

                // No internet connection available
                showToastMessage("you have no internet connection !!")
            }
        } else if (stopVpn()) {

            // VPN is stopped, show a Toast message.
            showToastMessage("Disconnect Successfully")
        }
    }

    fun stopVpn(): Boolean {
        try {
            preference!!.clear()
            OpenVPNThread.stop()
            status("connect")
            vpnStart = false
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            startVpn()
            startVpnFirebase()
        } else {
            showToastMessage("Permission deny !! ")
        }
    }

    fun getInternetStatus(): Boolean {
        return connection!!.netCheck(this)
    }

    fun isServiceRunning() {
        setStatus(OpenVPNService.getStatus())
    }

    private fun startVpnFirebase() {
        try {
            val database = FirebaseDatabase.getInstance()
            val serverARef = database.getReference("serverA")
            val serverBRef = database.getReference("serverB")
            val serverCRef = database.getReference("serverC")

            // Fetch the user counts for each server
            serverARef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val serverACount = dataSnapshot.childrenCount

                    serverBRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val serverBCount = dataSnapshot.childrenCount

                            serverCRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    val serverCCount = dataSnapshot.childrenCount

                                    // Determine which server has the lowest count
                                    val lowestCount =
                                        minOf(serverACount, serverBCount, serverCCount)

                                    // Dynamically determine the ovpnFileName based on the lowest count
                                    val ovpnFileName = when {
                                        serverACount == lowestCount -> "us.ovpn"
                                        serverBCount == lowestCount -> "korea.ovpn"
                                        else -> "japan.ovpn"
                                    }

                                    Log.d("TAG", "onDataChange:ovpnFileName $ovpnFileName ")
                                    // Now, you can use ovpnFileName to open the corresponding .ovpn file
                                    val conf = this@VpnConnectivity.assets.open(ovpnFileName)
                                    val isr = InputStreamReader(conf)
                                    val br = BufferedReader(isr)
                                    var config = ""
                                    var line: String?
                                    CoroutineScope(Dispatchers.IO).launch {
                                        while (true) {
                                            line = br.readLine()
                                            try {
                                                if (line!!.contains("remote")) {
                                                    hostName = line
                                                    Log.d("TAG", "startVpn: yes remote is here")
                                                }
                                            } catch (e: Exception) {
                                                // Handle exceptions
                                            }

                                            Log.d("TAG", "startVpn: read file " + line)
                                            if (line == null) break
                                            config += """
                                            $line
                                            
                                        """.trimIndent()
                                        }
                                        br.readLine()
                                        OpenVpnApi.startVpn(
                                            this@VpnConnectivity,
                                            config,
                                            server!!.country,
                                            server!!.ovpnUserName,
                                            server!!.ovpnUserPassword
                                        )
                                        // Update log
                                        // Update log
                                        // binding.logTv.setText("Connecting...")
                                        vpnStart = true
                                    }
                                    // Add data to the server with the lowest count
                                    when {
                                        serverACount == lowestCount -> {
                                            // Add data to serverA
                                            serverARef.push()
                                                .setValue(ServerModel(ovpnFileName, lowestCount))
                                        }

                                        serverBCount == lowestCount -> {
                                            // Add data to serverB
                                            serverBRef.push()
                                                .setValue(ServerModel(ovpnFileName, lowestCount))
                                        }

                                        else -> {
                                            // Add data to serverC
                                            serverCRef.push()
                                                .setValue(ServerModel(ovpnFileName, lowestCount))
                                        }
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    // Handle error
                                }
                            })

                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Handle error
                        }
                    })
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle error
                }
            })
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    private fun startVpn() {
        try {
            // .ovpn file
            Log.d("TAG", "startVpn: " + server!!.ovpn)
            val conf = server!!.ovpn?.let { this.assets.open(it) }
            val isr = InputStreamReader(conf)
            val br = BufferedReader(isr)
            var config = ""
            var line: String?
            CoroutineScope(Dispatchers.IO).launch {
                while (true) {
                    line = br.readLine()
                    try {
                        if (line!!.contains("remote")) {
                            hostName = line
                            Log.d("TAG", "startVpn: yes remote is here")
                        }
                    } catch (e: Exception) {

                    }

                    Log.d("TAG", "startVpn: read file " + line)
                    if (line == null) break
                    config += """
                $line
                
                """.trimIndent()

                }
                br.readLine()
                OpenVpnApi.startVpn(
                    this@VpnConnectivity,
                    config,
                    server!!.country,
                    server!!.ovpnUserName,
                    server!!.ovpnUserPassword
                )
                // Update log
                // Update log
                // binding.logTv.setText("Connecting...")
                vpnStart = true
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SetTextI18n")
    fun setStatus(connectionState: String?) {
        if (connectionState != null) when (connectionState) {
            "DISCONNECTED" -> {
                status("connect")
                vpnStart = false
                //  OpenVPNService.setDefaultStatus()
                Log.d("TAG", "setStatus:  connect")
                binding!!.txtConnectionStatus.text = "DisConnected"
//                binding!!.imgVpn.setImageResource(R.drawable.off_vpn)
                Glide.with(this)
                    .asBitmap()
                    .load(R.drawable.off_vpn)
                    .into(binding!!.imgVpn)
            }

            "CONNECTED" -> {
                vpnStart = true // it will use after restart this activity
                status("connected")
                // binding.logTv.setText("");
                binding!!.txtConnectionStatus.text = "Connected"
//                binding!!.imgVpn.setImageResource(R.drawable.on_vpn)
                Log.d("TAG", "setStatus:  connected")
                Glide.with(this)
                    .asBitmap()
                    .load(R.drawable.on_vpn)
                    .into(binding!!.imgVpn)
            }

            "WAIT" -> {                  //  binding.logTv.setText("waiting for server connection!!");
                Log.d("TAG", "setStatus:  waiting for server connection!!")
                binding!!.txtConnectionStatus.text = "waiting for server connection!!"
            }

            "AUTH" -> {
                binding!!.txtConnectionStatus.text = "server authenticating!!"
                //   binding.logTv.setText("server authenticating!!");
                Log.d("TAG", "setStatus:  server authenticating!!")
            }

            "RECONNECTING" -> {
                status("connecting")
                binding!!.txtConnectionStatus.text = "Reconnecting..."
                //   binding.logTv.setText("Reconnecting...");
                Log.d("TAG", "setStatus:  Reconnecting...")
            }

            "NONETWORK" -> {//     binding.logTv.setText("No network connection");
                Log.d("TAG", "setStatus:  No network connection")
                binding!!.txtConnectionStatus.text = "No network connection"
            }
        }
    }

    fun status(status: String) {
        if (status == "connect") {
            binding!!.txtConnectionStatus.text = "connect"
//            binding!!.imgVpn.setImageResource(R.drawable.on_vpn)
            Glide.with(this)
                .asBitmap()
                .load(R.drawable.on_vpn)
                .into(binding!!.imgVpn)
            // binding.vpnBtn.setText(this.getString(R.string.connect));
            Log.d("TAG", "status: " + this.getString(R.string.connect))
        } else if (status == "connecting") {
            binding!!.txtConnectionStatus.text = this.getString(R.string.connecting)
            Log.d("TAG", "status: " + this.getString(R.string.connecting))
            /*binding.vpnBtn.setText(getContext().getString(R.string.connecting));*/
        } else if (status == "connected") {
            Log.d("TAG", "status: " + this.getString(R.string.disconnect))

            //  binding.vpnBtn.setText(getContext().getString(R.string.disconnect));
        } else if (status == "tryDifferentServer") {

            //binding.vpnBtn.setBackgroundResource(R.drawable.button_connected);
            //  binding.vpnBtn.setText("Try Different\nServer");
            Log.d(
                "TAG", """
     status: Try Different
     Server
     """.trimIndent()
            )
        } else if (status == "loading") {
            //  binding.vpnBtn.setBackgroundResource(R.drawable.button);
            //binding.vpnBtn.setText("Loading Server..");
            Log.d("TAG", "status: " + "Loading Server..")
            binding!!.txtConnectionStatus.text = "Loading Server.."
        } else if (status == "invalidDevice") {
            // binding.vpnBtn.setBackgroundResource(R.drawable.button_connected);
            ////  binding.vpnBtn.setText("Invalid Device");
            Log.d("TAG", "status: " + "Invalid Device")
            binding!!.txtConnectionStatus.text = "Invalid Device"
        } else if (status == "authenticationCheck") {
            //  binding.vpnBtn.setBackgroundResource(R.drawable.button_connecting);
            //    binding.vpnBtn.setText("Authentication \n Checking...");
            Log.d("TAG", "\"Authentication \\n Checking...")
            binding!!.txtConnectionStatus.text = "\"Authentication \\n Checking..."
        }
    }

    var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                setStatus(intent.getStringExtra("state"))
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            try {
                var duration = intent.getStringExtra("duration")
                var lastPacketReceive = intent.getStringExtra("lastPacketReceive")
                var byteIn = intent.getStringExtra("byteIn")
                var byteOut = intent.getStringExtra("byteOut")
                if (duration == null) duration = "00:00:00"
                if (lastPacketReceive == null) lastPacketReceive = "0"
                if (byteIn == null) byteIn = " "
                if (byteOut == null) byteOut = " "
                updateConnectionStatus(duration, lastPacketReceive, byteIn, byteOut)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateConnectionStatus(
        duration: String,
        lastPacketReceive: String,
        byteIn: String,
        byteOut: String
    ) {
        Log.d("TAG", "updateConnectionStatus: Duration $duration")
        Log.d(
            "TAG",
            "updateConnectionStatus: Packet Received: $lastPacketReceive second ago"
        )
        Log.d("TAG", "updateConnectionStatus: Bytes In: $byteIn")
        Log.d("TAG", "updateConnectionStatus: Bytes Out: $byteOut")
        /* binding.durationTv.setText("Duration: " + duration);
        binding.lastPacketReceiveTv.setText("Packet Received: " + lastPacketReceive + " second ago");
        binding.byteInTv.setText("Bytes In: " + byteIn);
        binding.byteOutTv.setText("Bytes Out: " + byteOut);*/
    }

    fun showToastMessage(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun updateCurrentServerIcon(serverIcon: Int?) {
        /* Glide.with(this)
                .load(serverIcon)
                .into(binding.selectedServerIcon);*/
    }

    override fun onResume() {
        try {
            LocalBroadcastManager.getInstance(this)
                .registerReceiver(broadcastReceiver, IntentFilter("connectionState"))
//            server = preference!!.getServer()
//            binding!!.txtServerName!!.text = server!!.country
//            Glide.with(this)
//                .load(server!!.flagUrl)
//                .into(binding!!.imgServerFlag!!)
            super.onResume()
        } catch (e: Exception) {
        }
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
        super.onPause()
    }

    override fun onStop() {
//        if (server != null) {
//            preference!!.saveServer(server!!)
//        }
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        changeServer = this
    }

    override fun newServer(server: Server?) {
        this.server = server
        //updateCurrentServerIcon(server!!.flagUrl)
//        preference!!.saveServer(server!!)
        binding!!.txtServerName!!.text = server!!.country
        Glide.with(this)
            .load(server!!.flagUrl)
            .into(binding!!.imgServerFlag!!)
        if (vpnStart) {
            stopVpn()
        }
        prepareVpn()
    }

    /* override fun onItemClick(position: Int) {
         changeServer!!.newServer(serverLists!!.get(position));
         binding!!.txtServerName!!.text = serverLists!!.get(position).country
         Glide.with(this)
             .load(serverLists!!.get(position).flagUrl)
             .into(binding!!.imgServerFlag!!)

         dialog!!.dismiss()
         // binding!!.imgServerFlag!!.setImageResource(serverLists!!.get(position).flagUrl)
     }*/

    /* fun showServerDialog() {
         dialog = Dialog(this)
         dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
         dialog!!.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
         dialog!!.setContentView(R.layout.server_list_dialog)
         Log.d("TAG", "showServerDialog size: " + serverLists!!.size)
         var server_list_recycler: RecyclerView =
             dialog!!.findViewById(R.id.server_list_recycler) as RecyclerView
         server_list_recycler.layoutManager = LinearLayoutManager(this)
         server_list_recycler.adapter = ServerAdapter(serverLists!!, this, this)
         dialog!!.show()
     }*/

    /* fun checkHost() {
        Log.d("TAGCoroutine", "init: host main")
        for (i in serverLists!!) {
            try {
                // .ovpn file
                Log.d("TAG", "startVpn: " + server!!.ovpn)
                val conf = i.ovpn?.let { this.assets.open(it) }
                val isr = InputStreamReader(conf)
                val br = BufferedReader(isr)
                var line: String?
                while (true) {
                    line = br.readLine()
                    try {
                        if (line == null) break
                        if (line.contains("remote")) {
                            hostName = line
                            Log.d("TAG", "startVpn: yes remote is here")
                        }
                    } catch (e: Exception) {
                    }
                    Log.d("TAG", "startVpn: read file " + line)
                }
                Log.d("TAG", "checkHost: $hostName")
                val arr = hostName!!.split(" ").toTypedArray()
                //
                val receivedSever: AvailableServer = db!!.userDao().checkHost(arr[1])
                //   Log.d("TAG", "startVpn: check $receivedSever")
                // Log.d("TAG", "startVpn:check ping  ${receivedSever.Ping}")
                if (receivedSever != null) {
                    if (receivedSever.Ping!!.toInt() < 5) {

                        Log.d("connected vpn", "checkHost: connected to $hostName")
                        server =
                            Server(i.country, i.flagUrl, i.ovpn, i.ovpnUserName, i.ovpnUserPassword)
                    }
                }

            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }

    }*/
    override fun onBackPressed() {
//        startActivity(Intent(this, MainActivity::class.java))
        super.onBackPressed()
//        finish()
    }


}