package com.cindaku.holanear.db.converter

import androidx.room.TypeConverter
import com.cindaku.holanear.model.CallType
import com.cindaku.holanear.model.ChatType
import com.cindaku.holanear.model.MessageType
import java.util.*

open class ChatConverters{
    @TypeConverter
    open fun toMessageType(value: Int)= enumValues<MessageType>()[value]
    @TypeConverter
    open fun fromMessageType(value: MessageType)=value.ordinal
    @TypeConverter
    open fun toCallType(value: Int)= enumValues<CallType>()[value]
    @TypeConverter
    open fun fromCallType(value: CallType)=value.ordinal
    @TypeConverter
    open fun toChatType(value: Int)= enumValues<ChatType>()[value]
    @TypeConverter
    open fun fromChatType(value: ChatType)=value.ordinal
    @TypeConverter
    open fun toDate(date: Long): Date {
        return Date(date);
    }

    @TypeConverter
    open fun fromDate(date: Date): Long{
        return date.time
    }
}