package com.cindaku.holanear.db.entity

import androidx.room.*

@Entity
data class Group(
    @ColumnInfo(name = "group_id") @PrimaryKey(autoGenerate = true) var id: Int,
    @ColumnInfo(name = "contact_id") var contact_id: Int?
)