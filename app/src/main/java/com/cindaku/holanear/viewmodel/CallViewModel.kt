package com.cindaku.holanear.viewmodel

import androidx.lifecycle.ViewModel
import com.cindaku.holanear.db.entity.CallWithDetail
import com.cindaku.holanear.module.ChatRepository
import com.cindaku.holanear.module.Storage
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CallViewModel: ViewModel() {
    @Inject
    lateinit var chatRepository: ChatRepository
    @Inject
    lateinit var storage: Storage
    fun getAll(): Flow<List<CallWithDetail>> {
        return chatRepository.getAllCall()
    }
}