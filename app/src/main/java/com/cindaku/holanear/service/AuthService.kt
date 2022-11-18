package com.cindaku.holanear.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.cindaku.holanear.contact.KenulinAuthenticator
import javax.inject.Singleton

@Singleton
class AuthService: Service() {
    private lateinit var mAuthenticator: KenulinAuthenticator
    override fun onCreate() {
        super.onCreate()
        mAuthenticator=KenulinAuthenticator(this)
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}