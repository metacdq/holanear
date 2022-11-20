package com.cindaku.holanear

import android.app.Application
import android.content.Intent
import android.os.StrictMode
import com.cindaku.holanear.di.AppComponent
import com.cindaku.holanear.di.DaggerAppComponent
import com.cindaku.holanear.service.SIPService
import com.cindaku.holanear.service.XMPPService
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.google.GoogleEmojiProvider


open class BaseApp : Application(){
    private var serviceXMPP=false
    private var serviceSIP=false
    val appComponent: AppComponent by lazy {
        DaggerAppComponent.factory().create(applicationContext)
    }
    override fun onCreate() {
        super.onCreate()
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        EmojiManager.install(GoogleEmojiProvider())
    }
    open fun runXMPPService(){
       if(!serviceXMPP){
           Intent(this, XMPPService::class.java).also { intent ->
               startService(intent)
           }
           serviceXMPP=true
       }
    }
    open fun runSIPService(){
        if(!serviceSIP) {
            Intent(this, SIPService::class.java).also { intent ->
                startService(intent)
            }
            serviceSIP = true
        }
    }
}