package com.cindaku.holanear.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cindaku.holanear.db.entity.ChatListWithDetail
import com.cindaku.holanear.module.ChatRepository
import com.cindaku.holanear.module.Storage
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChatViewModel: ViewModel() {
    @Inject
    lateinit var chatRepository: ChatRepository
    @Inject
    lateinit var storage: Storage
    val selected= arrayListOf<ChatListWithDetail>()
    var isSelectionMode=false
    fun getAllChatList(): Flow<List<ChatListWithDetail>> {
        return chatRepository.getAllChatList()
    }
    fun deleteChat(){
        viewModelScope.coroutineContext.let {
            selected.forEach { detail ->
                chatRepository.deleteChat(detail.chatList.id!!)
            }
        }
    }
    fun toggelPin(){
        viewModelScope.coroutineContext.let {
            selected.forEach { detail->
                detail.chatList.let {
                    it.isPinned=!it.isPinned
                    chatRepository.updateChat(it)
                }
            }
        }
    }
    fun toggelMuted(){
        viewModelScope.coroutineContext.let {
            selected.forEach { detail->
                detail.chatList.let {
                    it.isMuted=!it.isMuted
                    chatRepository.updateChat(it)
                }
            }
        }
    }
    fun getUsername(): String{
        return storage.getString("jid")
    }
    fun offline(){
        storage.setBoolean("online",false)
    }
    fun online(){
        storage.setBoolean("online",true)
    }
}
