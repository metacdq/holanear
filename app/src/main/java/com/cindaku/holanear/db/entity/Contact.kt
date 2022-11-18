package com.cindaku.holanear.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Contact(
    @ColumnInfo(name = "contact_id") @PrimaryKey(autoGenerate = true) var id: Int?,
    @ColumnInfo(name = "fullname") var fullName: String?,
    @ColumnInfo(name = "nickname") var nickName: String?,
    @ColumnInfo(name = "phone") var phone: String?,
    @ColumnInfo(name = "jId") var jId: String?,
    @ColumnInfo(name = "contact_photo") var photo: String?,
    @ColumnInfo(name = "is_group") var isGrpup: Boolean=false,
    @ColumnInfo(name = "is_online") var isOnline: Boolean=false
)