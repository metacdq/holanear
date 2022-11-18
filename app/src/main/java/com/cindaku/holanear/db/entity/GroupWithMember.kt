package com.cindaku.holanear.db.entity


import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class GroupWithMember(
        @Embedded var group: Group,
        @Relation(
                parentColumn = "group_id",
                entity = Contact::class,
                entityColumn = "contact_id",
                associateBy = Junction(
                        value = GroupMember::class,
                        parentColumn = "group_id",
                        entityColumn = "contact_id"
                )
        )
        var members: List<Contact>,
        @Relation(
                parentColumn = "group_id",
                entityColumn = "group_id"
        )
        var groupContact: Contact
)