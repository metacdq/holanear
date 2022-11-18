package com.cindaku.holanear.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cindaku.holanear.model.CallType
import java.util.*

@Entity
data class Call(
    @ColumnInfo(name = "call_id") @PrimaryKey(autoGenerate = true) var id: Int?,
    @ColumnInfo(name = "contact_id") var caller: Int?,
    @ColumnInfo(name = "duration_sec") var duration: Int=0,
    @ColumnInfo(name = "type") var type: CallType=CallType.IN,
    @ColumnInfo(name = "is_ended") var ended: Boolean=false,
    @ColumnInfo(name = "call_date") var callDate: Date?
)