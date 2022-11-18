package com.cindaku.holanear.contact

import android.accounts.Account
import android.content.*
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import com.google.gson.Gson
import com.cindaku.holanear.BaseApp
import com.cindaku.holanear.db.entity.Contact
import com.cindaku.holanear.model.SyncRequest
import com.cindaku.holanear.provider.AUTHORITY

class SyncAdapter @JvmOverloads constructor(
    context: Context,
    autoInitialize: Boolean,
    allowParallelSyncs: Boolean = false,
    val mContentResolver: ContentResolver = context.contentResolver
) : AbstractThreadedSyncAdapter(context, autoInitialize, allowParallelSyncs) {
    override fun onPerformSync(
        account: Account?,
        extras: Bundle?,
        authority: String?,
        provider: ContentProviderClient?,
        syncResult: SyncResult?
    ) {
        if(authority==AUTHORITY){
            val listContact= arrayListOf<HashMap<String,String>>()
            val cursor: Cursor? =mContentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME
            )
            while (cursor!==null && cursor.moveToNext()){
                val contactId=cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                val name=cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                //Check phone number
                if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    val cursorPhone=mContentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID+" = ?",
                         arrayOf(contactId),
                       null
                    )
                    while (cursorPhone!==null && cursorPhone.moveToNext()){
                        val phone=cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).let {
                            it.replace("-","")
                                .replace("+","")
                                .replace(" ","")
                        }
                        Log.d("CONTACT", name+" "+ phone+" DETECTED")
                        val map=HashMap<String,String>()
                        map.put("phone",phone)
                        map.put("name",name)
                        listContact.add(map)
                    }
                    cursorPhone?.close()
                }
            }
            cursor?.close()
            val syncRequest= SyncRequest()
            syncRequest.phones= listContact
            Log.e("CONTACT",Gson().toJson(syncRequest))
            val response=(context.applicationContext as BaseApp).appComponent.kenulinService().sync(syncRequest).execute()
            if(response.code()==200){
                try{
                    response.body()!!.forEach {
                        val obj=it
                        val contact=Contact(
                            null,
                            obj.name,
                            "",
                            obj.phone,
                            obj.jid,
                            "",
                            false,
                            false
                        )
                        (context.applicationContext as BaseApp).appComponent.chatRepository().apply {
                            val check=this.getContactByPhone(obj.phone)
                            if(check===null){
                                this.addContact(contact)
                                Log.d("CONTACT", contact.fullName+" "+ contact.phone+" INSERTED")
                            }else{
                                check.fullName=obj.name
                                this.updateContact(contact)
                                Log.d("CONTACT", contact.fullName+" "+ contact.phone
                                        +" UPDATED")
                            }
                        }
                    }
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }else{
                Log.e("CONTAcT",response.code().toString()+" "+ String(response.errorBody()!!.bytes()))
            }
        }
    }

}