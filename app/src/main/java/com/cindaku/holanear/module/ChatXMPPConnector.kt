package com.cindaku.holanear.module

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import automatic
import com.google.gson.Gson
import com.cindaku.holanear.XMPP_HOST
import com.cindaku.holanear.db.entity.*
import com.cindaku.holanear.extension.genericType
import com.cindaku.holanear.model.*
import com.cindaku.holanear.ui.inf.OnChangeAvatar
import com.cindaku.holanear.utils.IDUtils
import com.cindaku.holanear.worker.DownloadWorker
import com.cindaku.holanear.worker.UploadWorker
import me.shouheng.compress.Compress
import org.jivesoftware.smack.*
import org.jivesoftware.smack.chat2.Chat
import org.jivesoftware.smack.chat2.ChatManager
import org.jivesoftware.smack.chat2.IncomingChatMessageListener
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.packet.MessageBuilder
import org.jivesoftware.smack.packet.Presence
import org.jivesoftware.smack.roster.Roster
import org.jivesoftware.smack.roster.RosterListener
import org.jivesoftware.smack.tcp.XMPPTCPConnection
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration
import org.jivesoftware.smackx.httpfileupload.HttpFileUploadManager
import org.jivesoftware.smackx.muc.InvitationListener
import org.jivesoftware.smackx.muc.MultiUserChat
import org.jivesoftware.smackx.muc.MultiUserChatManager
import org.jivesoftware.smackx.muc.packet.MUCUser
import org.jivesoftware.smackx.pubsub.PubSubManager
import org.jivesoftware.smackx.vcardtemp.VCardManager
import org.jivesoftware.smackx.xevent.MessageEventManager
import org.jivesoftware.smackx.xevent.MessageEventNotificationListener
import org.jxmpp.jid.EntityBareJid
import org.jxmpp.jid.EntityJid
import org.jxmpp.jid.Jid
import org.jxmpp.jid.impl.JidCreate
import org.jxmpp.jid.parts.Resourcepart
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatXMPPConnector  @Inject constructor(
    private val context: Context,
    private val storage: Storage,
    private val chatRepository: ChatRepository
): XMPPConnector,ConnectionListener,
    IncomingChatMessageListener,MessageEventNotificationListener,InvitationListener,RosterListener{
    private var xmpptcpConnection: XMPPTCPConnection?=null
    private lateinit var chatManager: ChatManager
    private lateinit var mucManager: MultiUserChatManager
    private lateinit var pubsubManager: PubSubManager
    private lateinit var vcardManager: VCardManager
    private lateinit var httpFileUploadManager: HttpFileUploadManager
    private lateinit var roster: Roster
    private lateinit var messageEventManager: MessageEventManager
    private var myJid=storage.getString("jid")+"@"+ XMPP_HOST
    override fun getMyJid(): String {
       return myJid
    }

    override fun connect() {
        try{
            if(xmpptcpConnection==null){
                SASLAuthentication.blacklistSASLMechanism("SCRAM-SHA-1");
                SASLAuthentication.blacklistSASLMechanism("DIGEST-MD5");
                SASLAuthentication.unBlacklistSASLMechanism("PLAIN");
                Log.d("XMPP","CONNECTING")
                val builder: XMPPTCPConnectionConfiguration.Builder =
                    XMPPTCPConnectionConfiguration.builder()
                builder.setXmppDomain(XMPP_HOST)
                builder.setPort(5222)
                builder.setResource("Mobile")
                builder.setSendPresence(true)
                builder.setSecurityMode(ConnectionConfiguration.SecurityMode.ifpossible)
                builder.setUsernameAndPassword(storage.getString("jid"), storage.getString("password"))
                xmpptcpConnection= XMPPTCPConnection(builder.build())
                xmpptcpConnection!!.addConnectionListener(this)
                xmpptcpConnection!!.connect()
                xmpptcpConnection!!.login()
                val reconnectionManager=ReconnectionManager.getInstanceFor(xmpptcpConnection)
                reconnectionManager.enableAutomaticReconnection()
                chatManager= ChatManager.getInstanceFor(xmpptcpConnection)
                pubsubManager= PubSubManager.getInstanceFor(xmpptcpConnection)
                mucManager= MultiUserChatManager.getInstanceFor(xmpptcpConnection)
                mucManager.setAutoJoinOnReconnect(true)
                roster=Roster.getInstanceFor(xmpptcpConnection)
                roster.subscriptionMode = Roster.SubscriptionMode.accept_all;
                roster.addRosterListener(this)
                chatManager.addIncomingListener(this)
                httpFileUploadManager= HttpFileUploadManager.getInstanceFor(xmpptcpConnection)
                messageEventManager= MessageEventManager.getInstanceFor(xmpptcpConnection)
                messageEventManager.addMessageEventNotificationListener(this)
            }else{
                if(!xmpptcpConnection!!.isConnected) {
                    xmpptcpConnection!!.connect()

                }
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun connected(connection: XMPPConnection?) {
        super.connected(connection)
        Log.d("XMPP","=========================CONNECTED TO XMPP=========================")
    }

    override fun connectionClosed() {
        super.connectionClosed()
        Log.d("XMPP","=========================DISCONNECT TO XMPP=========================")
        checkConnectiom()
    }
    override fun disconnect() {
        Log.d("XMPP","=========================DISCONNECT TO XMPP=========================")
        checkConnectiom()
    }

    override fun isConnected(): Boolean {
        if(xmpptcpConnection==null){
            return false
        }
        return xmpptcpConnection!!.isConnected
    }

    override fun createGroup(group: Group, members: GroupMember) {

    }

    override fun changeGroupInformation(group: GroupWithMember) {

    }

    override fun changeGroupProfilePicture(
        group: GroupWithMember,
        photo: File,
        callback: OnChangeAvatar
    ) {
        val jid = JidCreate.from(myJid)
        val muc=mucManager.getMultiUserChat(jid as EntityBareJid?)
        val message=MessageBuilder.buildMessage()
        muc.sendMessage(message)
    }


    override fun chatManager(): ChatManager {
        return chatManager
    }

    override fun deleteRemoteMessage(contact: Contact,messages: ArrayList<ChatMessage>) {
        checkConnectiom()
        try{
            checkConnectiom()
            try{
                contact.jId?.let {
                    var systemMessage=SystemMessage()
                    val deleted= arrayListOf<String>()
                    messages.forEach { message->
                        message.stanza_id?.let {
                            deleted.add(it)
                        }
                    }
                    systemMessage.message=Gson().toJson(deleted)
                    systemMessage.type=SystemMesageType.DELETE_MESSAGE
                    val chatMessage = ChatMessage(
                        null,
                        null,
                        myJid,
                        Gson().toJson(systemMessage),
                        "",
                        "",
                        "",
                        MessageType.SYSTEM,
                        Calendar.getInstance().time,
                        true,
                        false,
                        true,
                        false,
                        false,
                        false,
                        false,
                        contact.id,
                        null
                    )
                    val jidBareJid = JidCreate.from(it)
                    val xmppChat = chatManager.chatWith(jidBareJid as EntityBareJid?)
                    val xmppMessage = MessageBuilder.buildMessage()
                        .setBody(Gson().toJson(chatMessage)).build()
                    xmppMessage.stanzaId = IDUtils.generateMessageStanzaId(chatMessage)
                    xmppChat.send(xmppMessage)
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun forwardMessage(messages: ArrayList<ChatMessage>, to: List<Contact>) {
        checkConnectiom()
        messages.forEach { old->
            to.forEach { contact->
                var chat=chatRepository.findChatByJid(contact.jId!!)
                if(chat==null){
                    chat= ChatList(
                        null,
                        null,
                        Calendar.getInstance().time,
                        ChatType.PERSONAL,
                        contact.id,
                        false,
                        false,
                        null
                    )
                    chatRepository.addChat(chat)
                    chat=chatRepository.findChatByJid(contact.jId!!)
                }
                val chatMessage=ChatMessage(
                    null,
                    null,
                    myJid,
                    old.body!!,
                    "",
                    "",
                    old.attachmentName,
                    old.messageType,
                    Calendar.getInstance().time,
                    false,
                    false,
                    false,
                    false,
                    true,
                    false,
                    false,
                    contact.id,
                    chat!!.id
                )
                val jidBareJid = JidCreate.from(contact.jId!!)
                val xmppChat = chatManager.chatWith(jidBareJid as EntityBareJid?)
                val xmppMessage = MessageBuilder.buildMessage()
                    .setBody(Gson().toJson(chatMessage)).build()
                xmppMessage.stanzaId = IDUtils.generateMessageStanzaId(chatMessage)
                xmppChat.send(xmppMessage)
                chatMessage.stanza_id = xmppMessage.stanzaId
                chatRepository.addMessage(chatMessage)
                chat.lastMessage=chatMessage.id
                chat.lastUpdate = Calendar.getInstance().time
                chatRepository.updateChat(chat)
            }
        }
    }

    override fun roster(): Roster {
        return roster
    }

    override fun httpManager(): HttpFileUploadManager {
        return httpFileUploadManager
    }

    override fun mucManager(): MultiUserChatManager {
        return mucManager
    }

    override fun vcardManager(): VCardManager {
       return vcardManager
    }

    override fun changeProfilePicture(photo: File, callback: OnChangeAvatar) {
        try {
            val compress= Compress.with(context = context,file = photo)
                .setTargetDir(context.cacheDir.absolutePath).automatic().get()
            val url=httpFileUploadManager.uploadFile(compress.inputStream(),compress.nameWithoutExtension,compress.length())
            val jid = JidCreate.from(myJid)
            val vcard = vcardManager.loadVCard(jid as EntityBareJid?)
            vcard.setField("avatar",url.toString())
            vcardManager.saveVCard(vcard)
        }catch (e: Exception){
            e.printStackTrace()
            callback.onUpload(false)
        }
    }

    override fun changeNickname(name: String) {
        try {
            storage.setString("nickname",name)
            val jid = JidCreate.from(myJid)
            val vcard = vcardManager.loadVCard(jid as EntityBareJid?)
            vcard.nickName=name
            vcardManager.saveVCard(vcard)
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun changeStatus(status: String) {
        try {
            storage.setString("status",status)
            val jid = JidCreate.from(myJid)
            val vcard = vcardManager.loadVCard(jid as EntityBareJid?)
            vcard.setField("status",status)
            vcardManager.saveVCard(vcard)
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun sendReadEvent(contact: Contact) {
        checkConnectiom()
        try{
            val chatList=chatRepository.findChatByJid(contact.jId!!)
            chatList?.let {
                val messages=chatRepository.getUnReaded(it.id!!)
                messages.forEach {
                    if(it.stanza_id!="") {
                        val jidBareJid = JidCreate.from(contact.jId)
                        it.isSent=true
                        it.isReaded=true
                        chatRepository.updateMessage(it)
                        messageEventManager.sendDisplayedNotification(jidBareJid, it.stanza_id)
                    }
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun sendTextMessage(contact: Contact, text: String, reply: ReplySnippet?) {
        checkConnectiom()
        try{
            contact.jId?.let {
                var chat = chatRepository.findChatByJid(it)
                if (chat == null) {
                    chat = ChatList(
                        null,
                        null,
                        Calendar.getInstance().time,
                        ChatType.PERSONAL,
                        contact.id,
                        false,
                        false,
                        null
                    )
                    chatRepository.addChat(chat)
                    chat = chatRepository.findChatByJid(it)
                }
                val chatMessage = ChatMessage(
                    null,
                    null,
                    myJid,
                    text,
                    Gson().toJson(reply),
                    "",
                    "",
                    MessageType.TEXT,
                    Calendar.getInstance().time,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    contact.id,
                    chat!!.id
                )
                val jidBareJid = JidCreate.from(it)
                val xmppChat = chatManager.chatWith(jidBareJid as EntityBareJid?)
                val xmppMessage = MessageBuilder.buildMessage()
                    .setBody(Gson().toJson(chatMessage)).build()
                xmppMessage.stanzaId = IDUtils.generateMessageStanzaId(chatMessage)
                xmppChat.send(xmppMessage)
                chatMessage.stanza_id = xmppMessage.stanzaId
                chatMessage.id=chatRepository.addMessage(chatMessage)
                chat.lastMessage = chatMessage.id!!
                chat.lastUpdate = Calendar.getInstance().time
                chatRepository.updateChat(chat)
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun sendLocationMessage(
        contact: Contact,
        location: Location,
        image: File,
        reply: ReplySnippet?
    ) {
        checkConnectiom()
        contact.jId?.run {
            val detailMessage= LocationMessage()
            detailMessage.location = location
            detailMessage.url = ""
            var chat=chatRepository.findChatByJid(this)
            if(chat==null){
                chat= ChatList(
                    null,
                    null,
                    Calendar.getInstance().time,
                    ChatType.PERSONAL,
                    contact.id,
                    false,
                    false,
                    null
                )
                chatRepository.addChat(chat)
                chat=chatRepository.findChatByJid(this)
            }
            val chatMessage=ChatMessage(
                null,
                null,
                myJid,
                Gson().toJson(detailMessage),
                Gson().toJson(reply),
                image.path,
                location.description,
                MessageType.LOCATION,
                Calendar.getInstance().time,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                contact.id,
                chat!!.id
            )
            val messageId=chatRepository.addMessage(chatMessage)
            val uploadWorkRequest: WorkRequest =
                OneTimeWorkRequestBuilder<UploadWorker>()
                    .setInputData(workDataOf(
                        "ID" to messageId
                    ))
                    .build()
            WorkManager.getInstance(context)
                .enqueue(listOf(uploadWorkRequest))
            chatMessage.id=messageId
            chat.lastMessage=chatMessage.id!!
            chat.lastUpdate=Calendar.getInstance().time
            chatRepository.updateChat(chat)
        }
    }

    override fun sendDocumentMessage(
        contact: Contact,
        file: File,
        name: String,
        reply: ReplySnippet?
    ) {
        checkConnectiom()
        contact.jId?.run {
            val detailMessage= hashMapOf<String,String>()
            detailMessage["filename"] = name
            detailMessage["url"] = ""
            var chat=chatRepository.findChatByJid(this)
            if(chat==null){
                chat= ChatList(
                    null,
                    null,
                    Calendar.getInstance().time,
                    ChatType.PERSONAL,
                    contact.id,
                    false,
                    false,
                    null
                )
                chatRepository.addChat(chat)
                chat=chatRepository.findChatByJid(this)
            }
            val chatMessage=ChatMessage(
                null,
                null,
                myJid,
                Gson().toJson(detailMessage),
                Gson().toJson(reply),
                file.path,
                name,
                MessageType.FILE,
                Calendar.getInstance().time,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                contact.id,
                chat!!.id
            )
            val messageId=chatRepository.addMessage(chatMessage)
            val uploadWorkRequest: WorkRequest =
                OneTimeWorkRequestBuilder<UploadWorker>()
                    .setInputData(workDataOf(
                        "ID" to messageId
                    ))
                    .build()
            WorkManager.getInstance(context)
                .enqueue(listOf(uploadWorkRequest))
            chatMessage.id=messageId
            chat.lastMessage=chatMessage.id!!
            chat.lastUpdate=Calendar.getInstance().time
            chatRepository.updateChat(chat)
        }
    }

    override fun sendContactMessage(
        contact: Contact,
        attachment: ContactMessage,
        reply: ReplySnippet?
    ) {
        checkConnectiom()
        try{
            contact.jId?.let {
                var chat=chatRepository.findChatByJid(it)
                if(chat==null){
                    chat= ChatList(
                        null,
                        null,
                        Calendar.getInstance().time,
                        ChatType.PERSONAL,
                        contact.id,
                        false,
                        false,
                        null
                    )
                    chatRepository.addChat(chat)
                    chat=chatRepository.findChatByJid(it)
                }
                val chatMessage=ChatMessage(
                    null,
                    null,
                    myJid,
                    Gson().toJson(attachment),
                    Gson().toJson(reply),
                    attachment.name,
                    attachment.name,
                    MessageType.CONTACT,
                    Calendar.getInstance().time,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    contact.id,
                    chat!!.id
                )
                val jidBareJid=JidCreate.from(it)
                val xmppChat=chatManager.chatWith(jidBareJid as EntityBareJid?)
                val xmppMessage=MessageBuilder.buildMessage()
                    .setBody(Gson().toJson(chatMessage)).build()
                xmppMessage.stanzaId = IDUtils.generateMessageStanzaId(chatMessage)
                xmppChat.send(xmppMessage)
                chatMessage.stanza_id=xmppMessage.stanzaId
                chatMessage.id=chatRepository.addMessage(chatMessage)
                chat.lastMessage=chatMessage.id!!
                chat.lastUpdate=Calendar.getInstance().time
                chatRepository.updateChat(chat)
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun sendImageMessage(
        contact: Contact,
        data: ArrayList<HashMap<String, String>>,
        reply: ReplySnippet?
    ) {
        checkConnectiom()
        try{
            data.forEach {
                val images=it
                contact.jId?.run {
                    val detailMessage= hashMapOf<String,String>()
                    detailMessage["caption"] = images["caption"]!!
                    detailMessage["url"] = ""
                    var chat=chatRepository.findChatByJid(this)
                    if(chat==null){
                        chat= ChatList(
                            null,
                            null,
                            Calendar.getInstance().time,
                            ChatType.PERSONAL,
                            contact.id,
                            false,
                            false,
                            null
                        )
                        chatRepository.addChat(chat)
                        chat=chatRepository.findChatByJid(this)
                    }
                    val chatMessage=ChatMessage(
                        null,
                        null,
                        myJid,
                        Gson().toJson(detailMessage),
                        Gson().toJson(reply),
                        images["image"]!!,
                        images["caption"]!!,
                        MessageType.IMAGE,
                        Calendar.getInstance().time,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        contact.id,
                        chat!!.id
                    )
                   val messageId=chatRepository.addMessage(chatMessage)
                    val uploadWorkRequest: WorkRequest =
                        OneTimeWorkRequestBuilder<UploadWorker>()
                            .setInputData(workDataOf(
                                "ID" to messageId
                            ))
                            .build()
                    WorkManager.getInstance(context)
                        .enqueue(listOf(uploadWorkRequest))
                    chatMessage.id=messageId
                    chat.lastMessage= chatMessage.id
                    chat.lastUpdate=Calendar.getInstance().time
                    chatRepository.updateChat(chat)
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun sendVideoMessage(contact: Contact, video: String, reply: ReplySnippet?) {
        checkConnectiom()
        try{
            contact.jId?.run {
                val detailMessage= hashMapOf<String,String>()
                detailMessage["url"] = ""
                var chat=chatRepository.findChatByJid(this)
                if(chat==null){
                    chat= ChatList(
                        null,
                        null,
                        Calendar.getInstance().time,
                        ChatType.PERSONAL,
                        contact.id,
                        false,
                        false,
                        null
                    )
                    chatRepository.addChat(chat)
                    chat=chatRepository.findChatByJid(this)
                }
                val chatMessage=ChatMessage(
                    null,
                    null,
                    myJid,
                    Gson().toJson(detailMessage),
                    Gson().toJson(reply),
                    video,
                    "",
                    MessageType.VIDEO,
                    Calendar.getInstance().time,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    contact.id,
                    chat!!.id
                )
                val messageId=chatRepository.addMessage(chatMessage)
                val uploadWorkRequest: WorkRequest =
                    OneTimeWorkRequestBuilder<UploadWorker>()
                        .setInputData(workDataOf(
                            "ID" to messageId
                        ))
                        .build()
                WorkManager.getInstance(context)
                    .enqueue(listOf(uploadWorkRequest))
                chatMessage.id=messageId
                chat.lastMessage=chatMessage.id
                chat.lastUpdate=Calendar.getInstance().time
                chatRepository.updateChat(chat)
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun newIncomingMessage(from: EntityBareJid?, message: Message?, chat: Chat?) {
        try{
            if (message != null) {
                val jidString= from?.asEntityBareJidString()
                jidString?.let {
                    //ADD CONTACT IF NOT EXIST
                    var contact=chatRepository.findContactByJid(it)
                    if(contact==null){
                        chatRepository.createAnonymousContact(it)
                        contact=chatRepository.findContactByJid(it)
                    }
                    val old=Gson().fromJson<ChatMessage>(message.body!!,ChatMessage::class.java)
                    if(old.messageType!=MessageType.SYSTEM){
                        var chat=chatRepository.findChatByJid(jidString)
                        if(chat==null){
                            chat= ChatList(
                                null,
                                null,
                                Calendar.getInstance().time,
                                ChatType.PERSONAL,
                                contact!!.id,
                                false,
                                false,
                                null
                            )
                            chatRepository.addChat(chat)
                            chat=chatRepository.findChatByJid(jidString)
                        }
                        val chatMessage=ChatMessage(
                            null,
                            message.stanzaId,
                            old.sender,
                            old.body!!,
                            old.replyBody,
                            "",
                            old.attachmentName,
                            old.messageType,
                            Calendar.getInstance().time,
                            true,
                            true,
                            false,
                            false,
                            old.isForwarded,
                            false,
                            false,
                            contact!!.id,
                            chat!!.id
                        )
                        var lastMessage=""
                        if(old.messageType==MessageType.TEXT){
                            lastMessage=chatMessage.body!!
                        }else if(old.messageType==MessageType.IMAGE){
                            lastMessage="Send An Image"
                        }else if (old.messageType==MessageType.VIDEO){
                            lastMessage="Send An Video"
                        }else if (old.messageType==MessageType.FILE){
                            lastMessage="Send An File"
                        }else if (old.messageType==MessageType.CONTACT){
                            lastMessage="Send An Contact"
                        }else if (old.messageType==MessageType.LOCATION){
                            lastMessage="Share Location"
                        }else{
                            lastMessage="Send Message"
                        }
                        chatMessage.id=chatRepository.addMessage(chatMessage)
                        chat.lastMessage=chatMessage.id!!
                        chat.lastUpdate=Calendar.getInstance().time
                        chatRepository.updateChat(chat)
                        messageEventManager.sendDeliveredNotification(from,message.stanzaId)
                        if(!storage.getBoolean("online") && !chat.isMuted){
                            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                val importance = NotificationManager.IMPORTANCE_DEFAULT
                                val channel = NotificationChannel(chat.id.toString(), "chat-"+chat.id.toString(), importance)
                                notificationManager.createNotificationChannel(channel)
                            }
                            val builder = NotificationCompat.Builder(context, chat.id.toString())
                                .setSmallIcon(com.cindaku.holanear.R.drawable.ic_notif)
                                .setContentTitle(contact.fullName)
                                .setContentText(lastMessage)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            notificationManager.notify(chat.id!!,builder.build())
                        }
                        if(chatMessage.messageType==MessageType.IMAGE || chatMessage.messageType==MessageType.VIDEO){
                            val downloadWorker: WorkRequest =
                                OneTimeWorkRequestBuilder<DownloadWorker>()
                                    .setInputData(
                                        workDataOf(
                                            "ID" to chatMessage.id!!
                                        )
                                    )
                                    .build()
                            WorkManager.getInstance(context).enqueue(listOf(downloadWorker))
                        }
                    }else{
                        val systemMessage=Gson().fromJson<SystemMessage>(old.body!!,SystemMessage::class.java)
                        if(systemMessage.type.equals(SystemMesageType.DELETE_MESSAGE)){
                            val list=Gson().fromJson<List<String>>(
                                systemMessage.message,
                                genericType<List<String>>())
                            chatRepository.deleteFromSender(list)
                        }
                    }
                }
            }
        }catch (e: java.lang.Exception){
            e.printStackTrace()
        }
    }

    override fun cancelledNotification(from: Jid?, packetID: String?) {

    }

    override fun offlineNotification(from: Jid?, packetID: String?) {

    }

    override fun deliveredNotification(from: Jid?, packetID: String?) {
        packetID?.let {
            val message=chatRepository.getMessageByStanzaId(it)
            message.isSent=true
            message.sentDate=Calendar.getInstance().time
            chatRepository.updateMessage(message)
        }
    }

    override fun displayedNotification(from: Jid?, packetID: String?) {
        packetID?.let {
            val message=chatRepository.getMessageByStanzaId(it)
            message.isReaded=true
            chatRepository.updateMessage(message)
        }
    }

    override fun composingNotification(from: Jid?, packetID: String?) {

    }

    override fun checkConnectiom(){
        if(xmpptcpConnection==null){
            connect()
        }else if(!xmpptcpConnection!!.isConnected){
            xmpptcpConnection!!.connect()
        }
    }

    override fun invitationReceived(
        conn: XMPPConnection?,
        room: MultiUserChat?,
        inviter: EntityJid?,
        reason: String?,
        password: String?,
        message: Message?,
        invitation: MUCUser.Invite?
    ) {
        room?.let {
           val nick=Resourcepart.from(storage.getString("nickname"))
           it.join(nick)
        }
    }

    override fun entriesAdded(addresses: MutableCollection<Jid>?) {

    }

    override fun entriesUpdated(addresses: MutableCollection<Jid>?) {

    }

    override fun entriesDeleted(addresses: MutableCollection<Jid>?) {

    }

    override fun presenceChanged(presence: Presence?) {
        presence?.let {
            val contact=chatRepository.getContactByPhone(presence.from.toString())
            if(contact!=null) {
                val vcard = vcardManager.loadVCard(it.from as EntityBareJid?)
                val nickname = vcard.nickName
                val avatar = vcard.getField("avatar")
                val isOnline = it.isAvailable
                contact.nickName=nickname
                contact.photo=avatar
                contact.isOnline=isOnline
                chatRepository.updateContact(contact)
            }
        }
    }
}