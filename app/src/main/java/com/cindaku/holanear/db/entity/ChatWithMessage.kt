package com.cindaku.holanear.db.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ChatWithMessage(
        @Embedded var chatList: ChatList,
        @Relation(
                parentColumn = "chat_id",
                entityColumn = "chat_id"
        )
        var messages: List<ChatMessage>?
)