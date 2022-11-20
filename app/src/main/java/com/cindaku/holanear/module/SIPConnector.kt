package com.cindaku.holanear.module

import android.view.TextureView
import com.cindaku.holanear.db.entity.Contact
import com.cindaku.holanear.ui.inf.OnCall
import org.linphone.core.Core

interface SIPConnector {
    fun connect()
    fun checkConnectiom()
    fun makeACall(contact: Contact,isVideo: Boolean)
    fun addToGroupCall(contact: Contact)
    fun createGroupCall(contacts: List<Contact>)
    fun hangUp()
    fun getCore(): Core?
    fun accept()
    fun terminate()
    fun decline()
    fun toggleSpeaker(on: Boolean)
    fun toggleMic(on: Boolean)
    fun toggleVideoVoiceCall(on: Boolean)
    fun toggleCamera()
    fun disconnect()
    fun setOnCall(onCallInf: OnCall)
    fun setRemoteView(view: TextureView)
    fun setLocalView(view: TextureView)
    fun isConnected(): Boolean
}