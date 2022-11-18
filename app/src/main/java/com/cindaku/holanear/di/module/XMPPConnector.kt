package com.cindaku.holanear.di.module

import com.cindaku.holanear.db.entity.*
import com.cindaku.holanear.model.ContactMessage
import com.cindaku.holanear.model.Location
import com.cindaku.holanear.model.ReplySnippet
import com.cindaku.holanear.ui.inf.OnChangeAvatar
import org.jivesoftware.smack.chat2.ChatManager
import org.jivesoftware.smack.roster.Roster
import org.jivesoftware.smackx.httpfileupload.HttpFileUploadManager
import org.jivesoftware.smackx.muc.MultiUserChatManager
import org.jivesoftware.smackx.vcardtemp.VCardManager
import java.io.File


interface XMPPConnector {
    fun connect()
    fun disconnect()
    fun isConnected(): Boolean
    fun createGroup(group: Group,members: GroupMember)
    fun changeGroupInformation(group: GroupWithMember)
    fun changeGroupProfilePicture(group: GroupWithMember,photo: File,callback: OnChangeAvatar)
    fun getMyJid(): String
    fun checkConnectiom()
    fun chatManager(): ChatManager
    fun deleteRemoteMessage(contact: Contact,messages: ArrayList<ChatMessage>)
    fun forwardMessage(messages: ArrayList<ChatMessage>,to: List<Contact>)
    fun roster(): Roster
    fun httpManager(): HttpFileUploadManager
    fun mucManager(): MultiUserChatManager
    fun vcardManager(): VCardManager
    fun changeProfilePicture(photo: File,callback: OnChangeAvatar)
    fun changeNickname(name: String)
    fun changeStatus(status: String)
    fun sendReadEvent(contact: Contact)
    fun sendTextMessage(contact: Contact,text: String,reply: ReplySnippet?)
    fun sendLocationMessage(contact: Contact,location: Location,image: File,reply: ReplySnippet?)
    fun sendDocumentMessage(contact: Contact,file: File,name: String,reply: ReplySnippet?)
    fun sendContactMessage(contact: Contact,attachment: ContactMessage,reply: ReplySnippet?)
    fun sendImageMessage(contact: Contact,data: ArrayList<HashMap<String,String>>,reply: ReplySnippet?)
    fun sendVideoMessage(contact: Contact,video: String,reply: ReplySnippet?)
}