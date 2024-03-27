package com.example.kotlintest.vpn

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kotlintest.R

import com.google.android.material.card.MaterialCardView


class ServerAdapter(
    val serverList: ArrayList<Server>,
    val context: Context,
    val clickListener: ClickListener
) : RecyclerView.Adapter<ServerAdapter.MyViewHolder>() {
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var serverIcon: ImageView
        var serverCountry: TextView
        var serverItemLayout: MaterialCardView

        init {
            serverItemLayout = itemView.findViewById(R.id.serverItemLayout)
            serverIcon = itemView.findViewById(R.id.iconImg)
            serverCountry = itemView.findViewById(R.id.countryTv)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.server_list_view, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.serverCountry.setText(serverList.get(position).country)
        Glide.with(context)
            .load(serverList.get(position).flagUrl)
            .into(holder.serverIcon)
        holder.itemView.setOnClickListener {
            clickListener.onItemClick(position) }
    }

    override fun getItemCount(): Int {
        return serverList.size
    }

    interface ClickListener {
        fun onItemClick(position: Int)
    }
}