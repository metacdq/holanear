package com.cindaku.holanear.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["contact_id", "group_id"])
data class GroupMember(
        @ColumnInfo(name="contact_id") val contactId: Int,
        @ColumnInfo(name="group_id") val groupId: Int,
        @ColumnInfo(name="role") val role: String="user"
)