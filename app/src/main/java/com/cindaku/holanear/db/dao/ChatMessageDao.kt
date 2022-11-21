package com.cindaku.holanear.db.dao

import androidx.room.*
import com.cindaku.holanear.db.entity.ChatMessage
import kotlinx.coroutines.flow.Flow
@Dao
interface ChatMessageDao {
    @Query("select * from chatmessage where chat_id=:chat_id")
    fun findMessageByChatId(chat_id: Int): Flow<List<ChatMessage>>
    @Query("select * from chatmessage where chat_id=:chat_id")
    fun findMessageByChatIdFirst(chat_id: Int): List<ChatMessage>
    @Query("select * from chatmessage inner join chatlist on chatlist.chat_id=chatmessage.chat_id  " +
            "where ((body like :query and chat_type=0) or (attachment_name like :query and chat_type>0 and message_type<6)) " +
            "and chatlist.contact_id=:contact_id LIMIT 100")
    fun search(query: String,contact_id: Int): List<ChatMessage>
    @Query("select * from chatmessage where chat_id=:chat_id and is_left=1 and is_readed=0")
    fun getUnReadedMessage(chat_id: Int): List<ChatMessage>
    @Query("select * from chatmessage where message_id=:message_id")
    fun findMessageByMessageId(message_id: Int): ChatMessage
    @Query("select * from chatmessage where contact_id=:contact_id and sent_date>=:send")
    fun getLastMessageCurrentDateForContact(contact_id: Int,send: Long): ChatMessage?
    @Query("select * from chatmessage where stanza_id=:stanza_id")
    fun findMessageByStanzaId(stanza_id: String): ChatMessage?
    @Query("select * from chatmessage inner join chatlist on chatlist.chat_id=chatmessage.chat_id where chatlist.contact_id=:contact_id and (message_type<6 or message_type>6) order by sent_date ASC")
    fun findMessageByContactId(contact_id: Int): Flow<List<ChatMessage>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(message: ChatMessage): Long
    @Update
    fun update(message: ChatMessage)
    @Query("update chatmessage set is_star=1 where message_id in (:list)")
    fun stared(list: List<Int>)
    @Query("update chatmessage set is_star=0 where message_id in (:list)")
    fun unStared(list: List<Int>)
    @Query("update chatmessage set is_deleted=1  where message_id in (:list)")
    fun delete(list: List<Int>)
    @Query("update chatmessage set is_deleted=1  where stanza_id in (:list) and is_readed=1")
    fun deleteFromSender(list: List<String>)
    @Query("delete from chatmessage where chat_id=:chat_id")
    fun deleteAllMessage(chat_id: Int)
}
