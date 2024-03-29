package com.cindaku.holanear.service

import android.app.Service
import android.content.Intent
import android.os.*
import com.cindaku.holanear.BaseApp
import com.cindaku.holanear.module.Storage
import com.cindaku.holanear.module.XMPPConnector
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class XMPPService: Service() {
    @Inject
    lateinit var xmppConnector: XMPPConnector
    @Inject
    lateinit var storage: Storage
    override fun onCreate() {
        super.onCreate()
        (application as BaseApp).appComponent.inject(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val manager=getSystemService(POWER_SERVICE) as PowerManager
        manager.run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"chat:Kenulin").apply {
                acquire(10*60*1000L /*10 minutes*/)
                connect()
                release()
            }
        }
        return START_STICKY
    }

    fun connect(){
        if(storage.getBoolean("login")){
            xmppConnector.connect()
        }
    }

    private val binder: XMPPServiceBinder = XMPPServiceBinder()
    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    inner class XMPPServiceBinder : Binder() {
        fun getService(): XMPPService = this@XMPPService
    }
    companion object{
        fun getInstance()=XMPPService()
    }
}