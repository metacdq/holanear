package com.cindaku.holanear.viewmodel

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cindaku.holanear.XMPP_HOST
import com.cindaku.holanear.db.entity.Contact
import com.cindaku.holanear.module.ChatRepository
import com.cindaku.holanear.ui.inf.OnFinishAddContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    fun search(phone: String): List<Contact>{
        return chatRepository.searchByPhone(query = phone)
    }
    fun save(activity: AppCompatActivity, name: String, phone: String){
        viewModelScope.launch(Dispatchers.IO){
            val contacts = search(phone)
            if(contacts.isEmpty()){
                val toSave=Contact(
                    null,
                    name,
                    name,
                    phone,
                    "$phone@$XMPP_HOST",
                    ""
                )
                chatRepository.addContact(toSave)
                (activity as OnFinishAddContact).contactAdded(toSave)
            }else{
                viewModelScope.launch(Dispatchers.Main){
                    Toast.makeText(activity, "Contact Already Exists", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}