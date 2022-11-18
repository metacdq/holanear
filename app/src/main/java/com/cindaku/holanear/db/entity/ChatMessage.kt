package com.cindaku.holanear.db.entity

import androidx.room.*
import com.cindaku.holanear.model.MessageType
import java.util.*

@Entity
data class ChatMessage(
        @ColumnInfo(name = "message_id") @PrimaryKey(autoGenerate = false) var id: Int?,
        @ColumnInfo(name = "stanza_id") var stanza_id: String?,
        @ColumnInfo(name = "jid") var sender: String?,
        @ColumnInfo(name = "body") var body: String?,
        @ColumnInfo(name = "reply_body") var replyBody: String?,
        @ColumnInfo(name = "attachment") var attachment: String?,
        @ColumnInfo(name = "attachment_name") var attachmentName: String?,
        @ColumnInfo(name = "message_type") var messageType: MessageType?,
        @ColumnInfo(name = "sent_date") var sentDate: Date?,
        @ColumnInfo(name = "is_sent") var isSent: Boolean=false,
        @ColumnInfo(name = "is_left") var isLeft: Boolean=true,
        @ColumnInfo(name = "is_readed") var isReaded: Boolean=false,
        @ColumnInfo(name = "is_downloading") var isLoading: Boolean=false,
        @ColumnInfo(name = "is_forwarded") var isForwarded: Boolean=false,
        @ColumnInfo(name = "is_star") var isStar: Boolean=false,
        @ColumnInfo(name = "is_deleted") var isDeleted: Boolean=false,
        @ColumnInfo(name = "contact_id") var contactId: Int?,
        @ColumnInfo(name = "chat_id") var chatId: Int?
)