package com.cindaku.holanear.ui.inf

import com.cindaku.holanear.db.entity.ChatMessage
import com.cindaku.holanear.db.entity.Contact
import com.cindaku.holanear.model.ContactMessage
import com.cindaku.holanear.model.LocationMessage

interface OnMessageEvent {
    fun requestAddContact(contactToAdd: ContactMessage)
    fun onLongPressed(message: ChatMessage)
    fun onClick(message: ChatMessage)
    fun checkSelected(message: ChatMessage) : Boolean
    fun unSelectMessage(message: ChatMessage)
    fun selectMessage(message: ChatMessage)
    fun isSelectedMode(): Boolean
    fun clearSelection()
    fun getYourJid(): String
    fun goToContact(contact: Contact)
    fun getReplyContactName(jid: String): String
    fun onDownloadRequest(message: ChatMessage)
    fun onImageViewerRequest(message: ChatMessage)
    fun onVideoPlayRequest(message: ChatMessage)
    fun onGeoNavigate(message: LocationMessage)
    fun checkContact(phone: String): List<Contact>
    fun initReply()
    fun onRequestScrollToMessageId(messageId: String)
}