package com.cindaku.holanear.db.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class CallWithDetail (
    @Embedded var call: Call,
    @Relation(
        parentColumn = "call_id",
        entity = Contact::class,
        entityColumn = "contact_id",
        associateBy = Junction(
            value = CallMember::class,
            parentColumn = "call_id",
            entityColumn = "contact_id"
        )
    )
    var members: List<Contact>,
    @Relation(
        parentColumn = "contact_id",
        entityColumn = "contact_id"
    )
    var caller: Contact
)