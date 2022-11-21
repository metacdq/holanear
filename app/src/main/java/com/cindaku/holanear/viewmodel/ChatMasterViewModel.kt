package com.cindaku.holanear.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cindaku.holanear.module.SIPConnector
import com.cindaku.holanear.module.XMPPConnector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChatMasterViewModel: ViewModel() {
    @Inject
    lateinit var xmppConnector: XMPPConnector
    @Inject
    lateinit var sipConnector: SIPConnector

    fun  init(){
        viewModelScope.launch(Dispatchers.IO){
            try{
                xmppConnector.connect()
//                sipConnector.connect()
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }
}