package com.example.kotlintest.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import android.widget.Toast
import java.net.URL
import java.util.*

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val bundle = intent.extras
            val pdus = bundle?.get("pdus") as Array<Any>?
            if (pdus != null) {
                for (pdu in pdus) {
                    val smsMessage = SmsMessage.createFromPdu(pdu as ByteArray)
                    val messageBody = smsMessage.messageBody
                    val sender = smsMessage.displayOriginatingAddress
                    val links = extractLinks(messageBody)
                    Log.d("TAG", "onReceivelinks: links $links")
                    if (links.isNotEmpty()) {
                        processIncomingSmsWithLinks(context, messageBody, sender, links)
                    }else {
                    processIncomingSms(context, messageBody, sender)
                    }
                }
            }
        }
    }

     private fun processIncomingSms(context: Context?, message: String, sender: String) {
        val serviceIntent = Intent(context, MyBackgroundService::class.java)
        serviceIntent.action = MyBackgroundService.ACTION_PROCESS_SMS
        serviceIntent.putExtra(MyBackgroundService.EXTRA_SMS_MESSAGE, message)
        serviceIntent.putExtra(MyBackgroundService.EXTRA_SMS_SENDER, sender)
        context?.startService(serviceIntent)
    }


    private fun extractLinks(text: String): List<String> {
        val links = mutableListOf<String>()
        val words = text.split(" ")
        for (word in words) {
            if (word.startsWith("http://") || word.startsWith("https://") ||
                word.startsWith("www.") || word.contains(".com")
            ) {
                links.add(word)
            }
        }
        return links
    }

    private fun processIncomingSmsWithLinks(
        context: Context?,
        message: String,
        sender: String,
        links: List<String>
    ) {
        val serviceIntent = Intent(context, MyBackgroundService::class.java)
        serviceIntent.action = MyBackgroundService.ACTION_PROCESS_SMS_WITH_LINKS
        serviceIntent.putExtra(MyBackgroundService.EXTRA_SMS_MESSAGE, message)
        serviceIntent.putExtra(MyBackgroundService.EXTRA_SMS_SENDER, sender)
        serviceIntent.putStringArrayListExtra(MyBackgroundService.EXTRA_SMS_LINKS, ArrayList(links))
        context?.startService(serviceIntent)
    }
}
