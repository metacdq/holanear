package com.cindaku.holanear.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import com.cindaku.holanear.BaseApp
import com.cindaku.holanear.db.entity.Contact
import com.cindaku.holanear.di.module.ChatRepository

const val AUTHORITY = "com.kenulin.chatmessaging.provider"
const val SCHEME = "content://"
const val AUTHORITY_URI = SCHEME+ AUTHORITY
const val TABLE_PATH = "contact"
const val ACCOUNT_TYPE = "com.kenulin.chatmessaging"
const val SECONDS_PER_MINUTE = 60L
const val SYNC_INTERVAL_IN_MINUTES = 10L
const val SYNC_INTERVAL = SYNC_INTERVAL_IN_MINUTES * SECONDS_PER_MINUTE
val CONTENT_URI = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, TABLE_PATH)
class ContactContentProvider(): ContentProvider() {
    val KEY_FULLNAME="fullname"
    val KEY_PHONE="phone"
    val KEY_JID="jId"
    val KEY_PHOTO="contact_photo"
    lateinit var chatRepository: ChatRepository
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        try {
            val contact = Contact(
                null,
                values!!.getAsString(KEY_FULLNAME),
                "",
                values.getAsString(KEY_PHONE),
                values.getAsString(KEY_JID),
                values.getAsString(KEY_PHOTO),
                false,
                false
            )
            chatRepository.addContact(contact)
        }catch (e:Exception){
            e.printStackTrace()
        }
        return  uri
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
       return null
    }

    override fun onCreate(): Boolean {
        chatRepository=(context!!.applicationContext as BaseApp).appComponent.chatRepository()
        return true
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        if (selection != null) {
            val contact = Contact(
                selection?.toInt(),
                values!!.getAsString(KEY_FULLNAME),
                "",
                values.getAsString(KEY_PHONE),
                values.getAsString(KEY_JID),
                values.getAsString(KEY_PHOTO),
                false,
                false
            )
            return chatRepository.updateContact(contact)
        }else{
            return 0
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        if (selection != null) {
            return chatRepository.deleteContactById(id = selection.toInt())
        }else{
            return 0
        }
    }

    override fun getType(uri: Uri): String? {
       return ACCOUNT_TYPE
    }
}