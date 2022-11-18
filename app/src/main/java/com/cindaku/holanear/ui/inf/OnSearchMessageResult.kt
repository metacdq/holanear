package com.cindaku.holanear.ui.inf

import com.cindaku.holanear.db.entity.ChatMessage


interface OnSearchMessageResult {
    fun onResult(results: List<ChatMessage>)
    fun getNext(): Int?
    fun getPrevious(): Int?
    fun clearSearch()
}