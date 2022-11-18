package com.cindaku.holanear.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.cindaku.holanear.db.entity.ChatList
import com.cindaku.holanear.db.entity.ChatListWithDetail
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("select * from chatlist left join contact on contact.contact_id=chatlist.contact_id where contact.jId=:jid")
    fun findByJid(jid: String): ChatList?
    @Insert
    fun insert(chatList: ChatList)
    @Update
    fun update(chatList: ChatList)
    @Query("select *,(select count(message_id) from chatmessage where chat_id=chatlist.chat_id and is_readed=0 and is_left=1) as unread from chatlist order by is_pinned desc,last_date desc")
    fun all(): Flow<List<ChatListWithDetail>>
    @Query("delete from chatlist where chat_id=:chat_id")
    fun delete(chat_id: Int)
}