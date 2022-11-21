package com.cindaku.holanear.module

import android.annotation.SuppressLint
import com.cindaku.holanear.db.dao.CallDao
import com.cindaku.holanear.db.dao.ChatDao
import com.cindaku.holanear.db.dao.ChatMessageDao
import com.cindaku.holanear.db.dao.ContactDao
import com.cindaku.holanear.db.entity.*
import com.cindaku.holanear.model.MessageType
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
        private val contactDao: ContactDao,
        private val chatDao: ChatDao,
        private val callDao: CallDao,
        private val chatMessageDao: ChatMessageDao
): ChatRepository {
    override fun addContact(contact: Contact) {
        contactDao.insert(contact)
    }

    override fun addCall(call: Call) {
        val id=callDao.insert(call)
        val member=CallMember(
            call.caller!!,
            id.toInt()
        )
        callDao.insertMember(member)
    }

    override fun updateCall(call: Call) {
       callDao.update(call)
    }

    override fun findCallByContactId(contact_id: Int): Call? {
       return callDao.findCallByContactId(contact_id)
    }

    override fun deleteChat(chat_id: Int) {
        chatMessageDao.deleteAllMessage(chat_id = chat_id)
        chatDao.delete(chat_id = chat_id)
    }

    override fun addChat(chat: ChatList) {
        chatDao.insert(chat)
    }

    override fun getAllChatList(): Flow<List<ChatListWithDetail>> {
        return chatDao.all()
    }

    override fun getAllCall(): Flow<List<CallWithDetail>> {
        return callDao.allCall()
    }

    override fun getUnReaded(chat_id: Int): List<ChatMessage> {
        return chatMessageDao.getUnReadedMessage(chat_id)
    }

    override fun getMessageByChatId(chat_id: Int): Flow<List<ChatMessage>> {
        return chatMessageDao.findMessageByChatId(chat_id)
    }

    override fun getMessageByChatIdFirst(chat_id: Int): List<ChatMessage> {
        return chatMessageDao.findMessageByChatIdFirst(chat_id)
    }

    override fun getMessageByMessageId(message_id: Int): ChatMessage {
        return chatMessageDao.findMessageByMessageId(message_id)
    }

    override fun getMessageByStanzaId(stanza_id: String): ChatMessage? {
        return chatMessageDao.findMessageByStanzaId(stanza_id)
    }

    override fun getMessageByContactId(contact_id: Int): Flow<List<ChatMessage>> {
        return chatMessageDao.findMessageByContactId(contact_id)
    }

    override fun deleteMessage(ids: List<Int>) {
        chatMessageDao.delete(ids)
    }

    override fun deleteFromSender(ids: List<String>) {
        chatMessageDao.deleteFromSender(ids)
    }

    override fun starMessage(ids: List<Int>) {
        chatMessageDao.stared(ids)
    }

    override fun unStarMessage(ids: List<Int>) {
        chatMessageDao.unStared(ids)
    }

    override fun updateChat(chat: ChatList) {
        chatDao.update(chat)
    }

    override fun updateMessage(chatMessage: ChatMessage) {
        chatMessageDao.update(chatMessage)
    }

    @SuppressLint("SimpleDateFormat")
    override fun addMessage(chatMessage: ChatMessage): Int {
        val format=SimpleDateFormat("dd MMM yyyy")
        val date=format.format(Calendar.getInstance().time)
        val today=format.parse(date)
        val lastMessage=chatMessageDao.getLastMessageCurrentDateForContact(chatMessage.contactId!!,today.time)
        if(lastMessage==null){
            val dateMessage=ChatMessage(
                null,
                null,
                null,
                date,
                "",
                "",
                "",
                MessageType.DATE,
                Calendar.getInstance().time,
                true,
                false,
                true,
                false,
                false,
                false,
                false,
                chatMessage.contactId,
                chatMessage.chatId
            )
            chatMessageDao.insert(dateMessage)
            chatMessage.sentDate=Calendar.getInstance().time
        }
        return chatMessageDao.insert(chatMessage).toInt()
    }

    override fun createAnonymousContact(jid: String) {
        val namePart=jid.split("@")
        val contact=Contact(
                null,
                namePart[0],
                "",
                namePart[0],
                jid,
                "",
            false,
            false
        )
        contactDao.insert(contact)
    }

    override fun findChatByJid(jid: String): ChatList? {
        return chatDao.findByJid(jid)
    }

    override fun findContactByJid(jid: String): Contact? {
        return contactDao.getByJid(jid)
    }

    override fun findContactById(id: Int): Contact? {
        return contactDao.getById(id)
    }

    override fun searchByPhone(query: String): List<Contact> {
        return contactDao.get("%$query%")
    }

    override fun updateContact(contact: Contact): Int {
        return contactDao.update(contact)
    }

    override fun deleteContactById(id: Int): Int {
        return contactDao.deleteByID(id)
    }

    override fun getContactByPhone(phone: String): Contact? {
        return contactDao.getByPhone(phone)
    }

    override fun getContactByJid(jid: String): Contact? {
        return contactDao.getByJid(jid)
    }

    override fun listContactAll(): List<Contact> {
       return contactDao.all()
    }

    override fun searchMessage(query: String, contact_id: Int): List<ChatMessage> {
        return chatMessageDao.search("%"+query+"%",contact_id)
    }

}