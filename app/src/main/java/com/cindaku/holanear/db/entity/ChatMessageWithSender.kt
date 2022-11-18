package com.cindaku.holanear.db.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ChatMessageWithSender(
        @Embedded var message: ChatMessage,
        @Relation(
                parentColumn = "contact_id",
                entityColumn = "contact_id"
        )
        var sender: Contact?
)