package com.cindaku.holanear.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cindaku.holanear.DB_VERSION
import com.cindaku.holanear.db.converter.ChatConverters
import com.cindaku.holanear.db.dao.CallDao
import com.cindaku.holanear.db.dao.ChatDao
import com.cindaku.holanear.db.dao.ChatMessageDao
import com.cindaku.holanear.db.dao.ContactDao
import com.cindaku.holanear.db.entity.*

@Database(entities = [ChatList::class,Contact::class,Group::class,ChatMessage::class,GroupMember::class,Call::class,CallMember::class],version = DB_VERSION,exportSchema = false)
@TypeConverters(ChatConverters::class)
abstract class HolaNearDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun contactDao(): ContactDao
    abstract fun callDao(): CallDao
    abstract fun chatMessageDao(): ChatMessageDao
}