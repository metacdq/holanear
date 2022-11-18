package com.cindaku.holanear.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["contact_id", "call_id"])
data class CallMember(
    @ColumnInfo(name="contact_id") val contactId: Int,
    @ColumnInfo(name="call_id") val callId: Int
)