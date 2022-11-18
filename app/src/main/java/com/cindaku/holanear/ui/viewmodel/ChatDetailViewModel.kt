package com.cindaku.holanear.ui.viewmodel

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import com.cindaku.holanear.db.entity.ChatMessage
import com.cindaku.holanear.db.entity.Contact
import com.cindaku.holanear.di.module.ChatRepository
import com.cindaku.holanear.di.module.SIPConnector
import com.cindaku.holanear.di.module.Storage
import com.cindaku.holanear.di.module.XMPPConnector
import com.cindaku.holanear.model.ContactMessage
import com.cindaku.holanear.model.Location
import com.cindaku.holanear.model.MessageType
import com.cindaku.holanear.model.ReplySnippet
import com.cindaku.holanear.worker.DownloadWorker
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ChatDetailViewModel: ViewModel() {
    @Inject
    lateinit var xmppConnector: XMPPConnector
    @Inject
    lateinit var chatRepository: ChatRepository
    @Inject
    lateinit var sipConnector: SIPConnector
    @Inject
    lateinit var storage: Storage
    var contact: Contact?=null
    var selected: ArrayList<ChatMessage> = arrayListOf()
    var chatToReply: ReplySnippet?=null
    var isSelectMode=false
    fun generateSnippet(chatMessage: ChatMessage): ReplySnippet?{
        chatToReply= ReplySnippet()
        viewModelScope.coroutineContext.let {
            chatMessage.messageType.let {
                if(it==MessageType.IMAGE){
                    chatToReply?.snippetText="Image"
                }else if(it==MessageType.VIDEO){
                    chatToReply?.snippetText="Video"
                }else if(it==MessageType.FILE){
                    chatToReply?.snippetText="Document"
                }else if(it==MessageType.LOCATION){
                    chatToReply?.snippetText="Location"
                }else if(it==MessageType.CONTACT){
                    chatToReply?.snippetText="Contact"
                }else{
                    if(chatMessage.body.toString().length>60) {
                        chatToReply?.snippetText = chatMessage.body.toString().substring(0, 60)
                    }else{
                        chatToReply?.snippetText = chatMessage.body.toString()
                    }
                }
                chatToReply?.senderJid= chatMessage.sender.toString()
                chatToReply?.messageId= chatMessage.stanza_id.toString()
            }
        }
        return chatToReply
    }
    fun setReadedAll(){
        contact?.let {
            xmppConnector.sendReadEvent(it)
        }
    }
    fun call(isVideo: Boolean){
        contact?.let {
            sipConnector.makeACall(it,isVideo)
        }
    }
    fun getMessageByStanzaId(stanza_id: String): ChatMessage?{
        return chatRepository.getMessageByStanzaId(stanza_id)
    }
    fun deleteMessage(){
        viewModelScope.coroutineContext.let {
            val ids= arrayListOf<Int>()
            selected.forEach {
                it.id?.let { it1 -> ids.add(it1) }
            }
            chatRepository.deleteMessage(ids)
            xmppConnector.deleteRemoteMessage(contact!!,selected)
        }
    }
    fun forwardMessage(to: ArrayList<Contact>){
        viewModelScope.coroutineContext.let {
            xmppConnector.forwardMessage(selected,to.toList())
        }
    }
    fun starMessage(){
        viewModelScope.coroutineContext.let {
            val ids= arrayListOf<Int>()
            selected.forEach {
                it.id?.let { it1 -> ids.add(it1) }
            }
            chatRepository.starMessage(ids)
        }
    }
    fun unStarMessage(){
        viewModelScope.coroutineContext.let {
            val ids= arrayListOf<Int>()
            selected.forEach {
                it.id?.let { it1 -> ids.add(it1) }
            }
            chatRepository.starMessage(ids)
        }
    }
    fun search(phone: String): List<Contact>{
        return chatRepository.searchByPhone(query = phone)
    }
    fun searchMeassage(queryMessage: String): List<ChatMessage>{
        return chatRepository.searchMessage(queryMessage,contact_id = contact!!.id!!)
    }
    fun send(text: String){
        contact?.let {
            xmppConnector.sendTextMessage(it,text,chatToReply)
            chatToReply=null
        }
    }
    fun downloadMessage(context: Context,chatMessage: ChatMessage){
        viewModelScope.coroutineContext.let {
            val downloadWorker: WorkRequest =
                OneTimeWorkRequestBuilder<DownloadWorker>()
                    .setInputData(
                        workDataOf(
                        "ID" to chatMessage.id!!
                    )
                    )
                    .build()
            WorkManager.getInstance(context)
                .enqueue(listOf(downloadWorker))
        }
    }
    fun sendImage(images: ArrayList<HashMap<String,String>>){
        viewModelScope.coroutineContext.let {
            contact?.let {
                xmppConnector.sendImageMessage(it,images,reply = chatToReply)
                chatToReply=null
            }
        }
    }
    @SuppressLint("SimpleDateFormat")
    fun sendDocument(contentResolver: ContentResolver, uri: Uri, name: String, directory: File){
        viewModelScope.coroutineContext.let {
            contact?.let {
                val format= SimpleDateFormat("yyyyMMddHHmmss")
                val filename=format
                    .format(Calendar.getInstance().time)+"_"+name
                val file = File(directory.absolutePath+"/"+filename)
                file.outputStream().use {
                    contentResolver.openInputStream(uri)?.copyTo(it)
                }
                xmppConnector.sendDocumentMessage(it,file,name,chatToReply)
                chatToReply=null
            }
        }
    }
    fun sendLocation(location: Location,image: File){
        contact?.let {
            xmppConnector.sendLocationMessage(it,location,image,chatToReply)
            chatToReply=null
        }
    }
    fun sendVideo(video: String){
        contact?.let {
            xmppConnector.sendVideoMessage(it, video,chatToReply)
        }
    }
    fun sendContact(message: ContactMessage){
        viewModelScope.coroutineContext.let {
            contact?.let {
                xmppConnector.sendContactMessage(it, message,chatToReply)
                chatToReply=null
            }
        }
    }
    fun sendContactMultiple(message: List<ContactMessage>){
        viewModelScope.coroutineContext.let {
            contact?.let {
               message.forEach {
                   xmppConnector.sendContactMessage(contact!!, it,chatToReply)
                   chatToReply=null
               }
            }
        }
    }
    fun findMessageByContact(contact_id: Int): Flow<List<ChatMessage>> {
        return chatRepository.getMessageByContactId(contact_id)
    }
    fun getContactName(jid: String): String {
        if(jid==xmppConnector.getMyJid()){
            return "You"
        }
        var contact=chatRepository.findContactByJid(jid)
        if(contact==null){
            chatRepository.createAnonymousContact(jid)
            contact=chatRepository.findContactByJid(jid)
        }
        return contact!!.fullName!!
    }
    fun getTextReply(): String {
        chatToReply?.let {
            if(it.senderJid==xmppConnector.getMyJid()){
                return "You"
            }
            val contact=chatRepository.findContactByJid(it.senderJid)
            return contact!!.fullName!!
        }
        return ""
    }
    fun getSubTextReply(): String {
        chatToReply?.let {
           return it.snippetText
        }
        return ""
    }
    fun myJid(): String{
        return storage.getString("jid")
    }
}