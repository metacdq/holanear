package com.cindaku.holanear.service

import android.app.Service
import android.content.Intent
import android.os.*
import com.cindaku.holanear.BaseApp
import com.cindaku.holanear.di.module.SIPConnector
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SIPService : Service() {
    @Inject
    lateinit var connector: SIPConnector
    override fun onCreate() {
        super.onCreate()
        (application as BaseApp).appComponent.inject(this)
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(!connector.isConnected()){
            val manager=getSystemService(POWER_SERVICE) as PowerManager
            manager.run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"call:Kenulin").apply {
                    acquire(10*60*1000L /*10 minutes*/)
                    connector.connect()
                    release()
                }
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }
    private val binder: SIPService.SIPServiceBinder = SIPServiceBinder()
    inner class SIPServiceBinder : Binder() {
        fun getService(): SIPService = this@SIPService
    }
    companion object{
        fun getInstance()=SIPService()
    }
}