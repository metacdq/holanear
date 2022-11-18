package com.cindaku.holanear.db.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Relation

data class ChatListWithDetail(
        @Embedded var chatList: ChatList,
        @Relation(
                parentColumn = "contact_id",
                entityColumn = "contact_id"
        )
        var contact: Contact?,
        @Relation(
                parentColumn = "last_message",
                entityColumn = "message_id"
        )
        var lastMessage: ChatMessage?,
        @Relation(
                parentColumn = "group_id",
                entityColumn = "group_id"
        )
        var group: Group?,
        @ColumnInfo(name = "unread")
        var unread: Int=0
)