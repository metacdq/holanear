package com.cindaku.holanear.ui.inf

import com.cindaku.holanear.db.entity.Contact

interface OnFinishAddContact {
    fun contactAdded(contact: Contact)
}