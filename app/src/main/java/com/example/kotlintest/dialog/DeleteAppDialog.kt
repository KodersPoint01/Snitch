package com.example.kotlintest.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import com.example.kotlintest.databinding.DeleteAppDialogBinding

class DeleteAppDialog (private var mContext: Context, val callBacks: DeleteCallBack) :
    Dialog(mContext) {
    lateinit var binding: DeleteAppDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window!!.requestFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCancelable(true)
        binding = DeleteAppDialogBinding.inflate(LayoutInflater.from(mContext))
        setContentView(binding.root)


        binding.cardDelete.setOnClickListener {
            callBacks.onYesClick()
            dismiss()
        }
        binding.noBtn.setOnClickListener {
            dismiss()
        }
    }
}