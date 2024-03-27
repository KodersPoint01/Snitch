package com.example.kotlintest.vpn

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kotlintest.databinding.ActivityAvailableServerListBinding
import com.example.kotlintest.vpn.model.MainViewModelServer
import com.example.kotlintest.vpn.util.SharedPreference


class AvailableServerList : AppCompatActivity(), ServerAdapter.ClickListener {
    private var binding: ActivityAvailableServerListBinding? = null
    private var serverLists: ArrayList<Server>? = null
    private var preference: SharedPreference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAvailableServerListBinding.inflate(layoutInflater)
        val view: View = binding!!.root
        setContentView(view)
        serverLists = ArrayList()
        //serveritem= this
        preference = SharedPreference(this)

        binding!!.serverListRecycler.layoutManager = LinearLayoutManager(this)
        val model: MainViewModelServer by viewModels()
        model.populateServerList().observe(this) {
            serverLists = it
            Log.d("TAG", "onCreate all serverLists: $serverLists")
            binding!!.serverListRecycler.adapter =
                ServerAdapter(serverLists!!, this, this)
        }
        binding!!.imgServerListBackBtn.setOnClickListener {
            super.onBackPressed()
            finish()
        }
    }


    override fun onItemClick(position: Int) {
        preference!!.saveServer(serverLists!![position])
        val intent = Intent(this, VpnConnectivity::class.java)
        intent.putExtra("currentServer", serverLists!![position])
        Log.d("TAG", "onItemClick: server $" + serverLists!![position])
        startActivity(intent)
        super.onBackPressed()
        finish()
    }

    override fun onResume() {
        super.onResume()

    }
}