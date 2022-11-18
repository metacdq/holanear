package com.cindaku.holanear.activity

import android.accounts.Account
import android.content.ContentResolver
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cindaku.holanear.BaseApp
import com.cindaku.holanear.R
import com.cindaku.holanear.di.module.Storage
import com.cindaku.holanear.provider.AUTHORITY
import com.cindaku.holanear.provider.SYNC_INTERVAL
import javax.inject.Inject

class MainActivity : AppCompatActivity() {
    val permissionList= arrayOf(
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.WRITE_CONTACTS,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    @Inject
    lateinit var storage: Storage
    lateinit var mResolver: ContentResolver
    private var account: Account?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        (application as BaseApp).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermissions(permissionList,1001)
    }
    fun checkLogin(){
       if(storage.getBoolean("login")){
           try {
               (application as BaseApp).runSyncService()
               (application as BaseApp).runXMPPService()
               (application as BaseApp).runSIPService()
               mResolver = contentResolver
               account=(application as BaseApp).appComponent.account()
               bindResolver()
               sync()
               val intent = Intent(baseContext, ChatActivity::class.java)
               startActivity(intent)
               finish()
           }catch (e: Exception){
               e.printStackTrace()
           }
       }else{
           val intent=Intent(baseContext,LoginActivity::class.java)
           startActivity(intent)
           finish()
       }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==1001){
            if(permissions.size==grantResults.size){
                checkLogin()
            }else{
                finish()
            }
        }
    }
    fun sync(){
        account?.let {
            val settingsBundle = Bundle().apply {
                putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)
                putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)
            }
            ContentResolver.requestSync(account, AUTHORITY, settingsBundle)
        }
    }
    fun bindResolver() {
        account?.let {
            ContentResolver.setSyncAutomatically(
                account,
                AUTHORITY,
                true)
            ContentResolver.addPeriodicSync(
                account,
                AUTHORITY,
                Bundle.EMPTY,
                SYNC_INTERVAL
            )
        }
    }
}