package com.cindaku.holanear.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import automatic
import com.google.gson.Gson
import com.cindaku.holanear.BaseApp
import com.cindaku.holanear.model.LocationMessage
import com.cindaku.holanear.model.MessageType
import com.cindaku.holanear.utils.IDUtils
import me.shouheng.compress.Compress
import org.jivesoftware.smack.packet.MessageBuilder
import org.jxmpp.jid.EntityBareJid
import org.jxmpp.jid.impl.JidCreate
import java.io.File
import java.lang.Exception
import java.util.*
import kotlin.collections.HashMap

class UploadWorker(val context: Context, private val workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val messageId=workerParams.inputData.getInt("ID",0)
        if(messageId==0){
            return Result.success()
        }
        try {
            val message=(applicationContext as BaseApp).appComponent.chatRepository().getMessageByMessageId(messageId)
            if(message.messageType==MessageType.IMAGE){
                if(message.isSent){
                    return Result.success()
                }else{
                    val detailMessage=Gson().fromJson(message.body!!, HashMap<String,String>()::class.java)
                    val file=File(message.attachment!!)
                    val compress=Compress.with(context = context,file = file)
                        .setTargetDir(context.cacheDir.absolutePath)
                        .automatic().get()
                    val url=(applicationContext as BaseApp).appComponent
                        .xmppConnector()
                        .httpManager().uploadFile(compress.inputStream(),compress.nameWithoutExtension,compress.length())
                    detailMessage["url"]=url.toExternalForm()
                    message.sentDate=Calendar.getInstance().time
                    message.body=Gson().toJson(detailMessage)
                    val contact= (applicationContext as BaseApp).appComponent
                        .chatRepository().findContactById(message.contactId!!)
                    contact?.let {
                        val jidBareJid= JidCreate.from(it.jId)
                        val xmppChat=(applicationContext as BaseApp).appComponent.xmppConnector().chatManager()
                            .chatWith(jidBareJid as EntityBareJid)
                        val xmppMessage= MessageBuilder.buildMessage()
                            .setBody(Gson().toJson(message)).build()
                        xmppMessage.stanzaId = IDUtils.generateMessageStanzaId(message)
                        xmppChat.send(xmppMessage)
                        message.stanza_id=xmppMessage.stanzaId
                    }
                    (applicationContext as BaseApp).appComponent.chatRepository().updateMessage(chatMessage = message)
                    return Result.success()
                }
            }else if(message.messageType==MessageType.LOCATION){
                if(message.isSent){
                    return Result.success()
                }else{
                    val detailMessage=Gson().fromJson(message.body!!, LocationMessage::class.java)
                    val file=File(message.attachment!!)
                    val url=(applicationContext as BaseApp).appComponent
                        .xmppConnector()
                        .httpManager().uploadFile(file.inputStream(),file.nameWithoutExtension,file.length())
                    detailMessage.url=url.toExternalForm()
                    message.sentDate=Calendar.getInstance().time
                    message.body=Gson().toJson(detailMessage)
                    val contact= (applicationContext as BaseApp).appComponent
                        .chatRepository().findContactById(message.contactId!!)
                    contact?.let {
                        val jidBareJid= JidCreate.from(it.jId)
                        val xmppChat=(applicationContext as BaseApp).appComponent.xmppConnector().chatManager()
                            .chatWith(jidBareJid as EntityBareJid)
                        val xmppMessage= MessageBuilder.buildMessage()
                            .setBody(Gson().toJson(message)).build()
                        xmppMessage.stanzaId = IDUtils.generateMessageStanzaId(message)
                        xmppChat.send(xmppMessage)
                        message.stanza_id=xmppMessage.stanzaId
                    }
                    (applicationContext as BaseApp).appComponent.chatRepository().updateMessage(chatMessage = message)
                    return Result.success()
                }
            }else{
                if(message.isSent){
                    return Result.success()
                }else{
                    val detailMessage=Gson().fromJson(message.body!!, HashMap<String,String>()::class.java)
                    val file=File(message.attachment!!)
                    val url=(applicationContext as BaseApp).appComponent
                        .xmppConnector()
                        .httpManager().uploadFile(file.inputStream(),file.nameWithoutExtension,file.length())
                    detailMessage["url"]=url.toExternalForm()
                    message.sentDate=Calendar.getInstance().time
                    message.body=Gson().toJson(detailMessage)
                    val contact= (applicationContext as BaseApp).appComponent
                        .chatRepository().findContactById(message.contactId!!)
                    contact?.let {
                        val jidBareJid= JidCreate.from(it.jId)
                        val xmppChat=(applicationContext as BaseApp).appComponent.xmppConnector().chatManager()
                            .chatWith(jidBareJid as EntityBareJid)
                        val xmppMessage= MessageBuilder.buildMessage()
                            .setBody(Gson().toJson(message)).build()
                        xmppMessage.stanzaId = IDUtils.generateMessageStanzaId(message)
                        xmppChat.send(xmppMessage)
                        message.stanza_id=xmppMessage.stanzaId
                    }
                    (applicationContext as BaseApp).appComponent.chatRepository().updateMessage(chatMessage = message)
                    return Result.success()
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
        return Result.failure()
    }
}