package com.cindaku.holanear

import android.app.Application
import android.content.Intent
import android.os.StrictMode
import com.cindaku.holanear.di.AppComponent
import com.cindaku.holanear.di.DaggerAppComponent
import com.cindaku.holanear.service.XMPPService
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.google.GoogleEmojiProvider
import org.minidns.dnsserverlookup.android21.AndroidUsingLinkProperties


open class BaseApp : Application(){
    private var serviceXMPP=false
    val appComponent: AppComponent by lazy {
        DaggerAppComponent.factory().create(applicationContext)
    }
    override fun onCreate() {
        super.onCreate()
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        EmojiManager.install(GoogleEmojiProvider())
        AndroidUsingLinkProperties.setup(applicationContext);
    }
    open fun runXMPPService(){
       if(!serviceXMPP){
           Intent(this, XMPPService::class.java).also { intent ->
               startService(intent)
           }
           serviceXMPP=true
       }
    }
}