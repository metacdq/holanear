package com.cindaku.holanear.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.cindaku.holanear.BaseApp
import com.cindaku.holanear.R
import com.cindaku.holanear.ui.inf.OnLoading
import com.cindaku.holanear.viewmodel.MainViewModel

class MainActivity : AppCompatActivity(), OnLoading {
    val permissionList= arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    lateinit var mainViewModel: MainViewModel
    private lateinit var buttonLogin : Button
    private lateinit var progressBar: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        mainViewModel=ViewModelProvider(this)[MainViewModel::class.java]
        (application as BaseApp).appComponent.inject(mainViewModel)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        buttonLogin=findViewById(R.id.buttonLogin)
        progressBar=findViewById(R.id.progressBar)
        buttonLogin.setOnClickListener {
            mainViewModel.login(this)
        }
    }

    override fun onStart() {
        super.onStart()
        requestPermissions(permissionList,1001)
    }
    fun checkLogin(){
        showLoading()
       if(mainViewModel.checkLogin()){
           try {
               hideLoading()
               val intent = Intent(baseContext, ChatActivity::class.java)
               startActivity(intent)
               finish()
           }catch (e: Exception){
               e.printStackTrace()
           }
       }else{
           hideLoading()
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

    override fun showLoading() {
        progressBar.isVisible=true
        buttonLogin.isVisible=false
    }

    override fun hideLoading() {
        progressBar.isVisible=false
        buttonLogin.isVisible=true
    }
}