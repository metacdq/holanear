package com.cindaku.holanear.ui.inf

import com.cindaku.holanear.db.entity.ChatListWithDetail

interface OnChatListEvent {
    fun onClickItem(chatMessaListWithDetail: ChatListWithDetail)
    fun onAdd(chatMessaListWithDetail: ChatListWithDetail)
    fun onRemove(chatMessaListWithDetail: ChatListWithDetail)
    fun isSelected(chatMessaListWithDetail: ChatListWithDetail): Boolean
    fun isSelectionMode(): Boolean
    fun clear()
    fun deleteChat()
    fun toggelMuted()
    fun toggelPin()
    fun getTotalSelection(): Int
}