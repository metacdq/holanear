package com.cindaku.holanear.db.entity

import androidx.room.*
import com.cindaku.holanear.model.ChatType
import java.util.*

@Entity
data class ChatList(
        @ColumnInfo(name = "chat_id")  @PrimaryKey(autoGenerate = true) var id: Int?,
        @ColumnInfo(name = "last_message") var lastMessage: Int?,
        @ColumnInfo(name = "last_date") var lastUpdate: Date?,
        @ColumnInfo(name = "chat_type") var chatType: ChatType?,
        @ColumnInfo(name = "contact_id") var contactId: Int?,
        @ColumnInfo(name = "is_muted") var isMuted: Boolean=false,
        @ColumnInfo(name = "is_pinned") var isPinned: Boolean=false,
        @ColumnInfo(name = "group_id") var groupId: Int?
)