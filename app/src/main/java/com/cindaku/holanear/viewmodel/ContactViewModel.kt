package com.cindaku.holanear.viewmodel

import androidx.lifecycle.ViewModel
import com.cindaku.holanear.db.entity.Contact
import com.cindaku.holanear.module.ChatRepository
import javax.inject.Inject

class ContactViewModel : ViewModel() {
    @Inject
    lateinit var chatRepository: ChatRepository
    var selected= arrayListOf<Contact>()
    var isSelectionMode=false
    var toSend=true
    fun getContact(): List<Contact>{
        return chatRepository.listContactAll()
    }
}