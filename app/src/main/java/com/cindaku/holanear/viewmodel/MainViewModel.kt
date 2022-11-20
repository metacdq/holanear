package com.cindaku.holanear.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.cindaku.holanear.activity.MainActivity
import com.cindaku.holanear.module.Storage
import com.knear.android.OnReceiveUri
import com.knear.android.service.NearMainService
import javax.inject.Inject

class MainViewModel : ViewModel() {
    @Inject
    lateinit var storage: Storage
    @Inject
    lateinit var nearMainService: NearMainService
    fun checkLogin(): Boolean{
        return storage.getBoolean("login")
    }

    fun login(activity: MainActivity){
        nearMainService.login(activity, object: OnReceiveUri{
            override fun onReceive(uri: Uri) {
                Log.d("KEY", nearMainService.viewAccessKey().toString())
            }

        })
    }
}