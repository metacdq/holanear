package com.cindaku.holanear.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cindaku.holanear.BaseApp

class OnBootReceiver: BroadcastReceiver() {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(p0: Context?, p1: Intent?) {
        p0?.let {
            try {
                (it.applicationContext as BaseApp).runXMPPService()
//                (it.applicationContext as BaseApp).runSIPService()
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }
}