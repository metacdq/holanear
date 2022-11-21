package com.cindaku.holanear.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cindaku.holanear.module.Storage
import com.cindaku.holanear.module.XMPPConnector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingViewModel : ViewModel() {
    @Inject
    lateinit var storage: Storage
    @Inject
    lateinit var xmppConnector: XMPPConnector
    fun getJid(): String{
        return storage.getString("jid")
    }

    fun processLogout(){
        viewModelScope.launch(Dispatchers.IO){
            storage.setBoolean("login",false)
            storage.setString("jid", "")
            storage.setString("password", "")
            xmppConnector.disconnect()
        }
    }
}