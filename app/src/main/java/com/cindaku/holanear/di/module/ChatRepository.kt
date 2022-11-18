package com.cindaku.holanear.di.module

import com.cindaku.holanear.db.entity.*
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun addContact(contact: Contact)
    fun addCall(call: Call)
    fun updateCall(call: Call)
    fun findCallByContactId(contact_id: Int): Call?
    fun deleteChat(chat_id: Int)
    fun addChat(chat: ChatList)
    fun getAllChatList(): Flow<List<ChatListWithDetail>>
    fun getAllCall(): Flow<List<CallWithDetail>>
    fun getUnReaded(chat_id: Int): List<ChatMessage>
    fun getMessageByChatId(chat_id: Int): Flow<List<ChatMessage>>
    fun getMessageByChatIdFirst(chat_id: Int): List<ChatMessage>
    fun getMessageByMessageId(message_id: Int): ChatMessage
    fun getMessageByStanzaId(stanza_id: String): ChatMessage
    fun getMessageByContactId(contact_id: Int): Flow<List<ChatMessage>>
    fun deleteMessage(ids: List<Int>)
    fun deleteFromSender(ids: List<String>)
    fun starMessage(ids: List<Int>)
    fun unStarMessage(ids: List<Int>)
    fun updateChat(chat: ChatList)
    fun updateMessage(chatMessage: ChatMessage)
    fun addMessage(chatMessage: ChatMessage): Int
    fun createAnonymousContact(jid: String)
    fun findChatByJid(jid: String): ChatList?
    fun findContactByJid(jid: String): Contact?
    fun findContactById(id: Int): Contact?
    fun searchByPhone(query: String): List<Contact>
    fun updateContact(contact: Contact): Int
    fun deleteContactById(id: Int): Int
    fun getContactByPhone(phone: String): Contact?
    fun getContactByJid(jid: String): Contact?
    fun listContactAll(): List<Contact>
    fun searchMessage(query: String,contact_id: Int): List<ChatMessage>
}