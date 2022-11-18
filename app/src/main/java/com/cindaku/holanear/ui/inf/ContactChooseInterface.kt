package com.cindaku.holanear.ui.inf

import com.cindaku.holanear.db.entity.Contact

interface ContactChooseInterface {
    fun onAdd(contact: Contact)
    fun onRemove(contact: Contact)
    fun onSingle(contact: Contact)
    fun isSelectionMode(): Boolean
    fun isSelectedContact(contact: Contact): Boolean
}