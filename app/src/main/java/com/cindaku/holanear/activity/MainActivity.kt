package com.cindaku.holanear.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.cindaku.holanear.BaseApp
import com.cindaku.holanear.R
import com.cindaku.holanear.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    val permissionList= arrayOf(
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.WRITE_CONTACTS,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    lateinit var mainViewModel: MainViewModel
    private lateinit var buttonLogin : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        mainViewModel=ViewModelProvider(this)[MainViewModel::class.java]
        (application as BaseApp).appComponent.inject(mainViewModel)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        buttonLogin=findViewById(R.id.buttonLogin)
        buttonLogin.setOnClickListener {
            mainViewModel.login(this)
        }
    }

    override fun onStart() {
        super.onStart()
        requestPermissions(permissionList,1001)
    }
    fun checkLogin(){
       if(mainViewModel.checkLogin()){
           try {
               lifecycleScope.launch(Dispatchers.Main){
                   (application as BaseApp).runXMPPService()
                   (application as BaseApp).runSIPService()
               }
               val intent = Intent(baseContext, ChatActivity::class.java)
               startActivity(intent)
               finish()
           }catch (e: Exception){
               e.printStackTrace()
           }
       }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==1001){
            if(permissionList.size==grantResults.size){
                checkLogin()
            }else{
                finish()
            }
        }
    }

}