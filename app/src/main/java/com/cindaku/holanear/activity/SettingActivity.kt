package com.cindaku.holanear.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.cindaku.holanear.BaseApp
import com.cindaku.holanear.R
import com.cindaku.holanear.ui.drawable.AcronymDrawable
import com.cindaku.holanear.viewmodel.SettingViewModel

class SettingActivity : AppCompatActivity() {
    lateinit var toolbar: Toolbar
    lateinit var back: ImageView
    lateinit var walletTextView: TextView
    lateinit var buttonLogout: Button
    lateinit var imageViewSetting: ImageView
    lateinit var viewModel: SettingViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[SettingViewModel::class.java]
        (application as BaseApp).appComponent.inject(viewModel)
        setContentView(R.layout.activity_setting)
        toolbar=findViewById(R.id.toolbar)
        back=findViewById(R.id.backImageView)
        walletTextView=findViewById(R.id.textViewWalletAdress)
        buttonLogout=findViewById(R.id.buttonLogout)
        imageViewSetting=findViewById(R.id.imageViewSetting)
        back.setOnClickListener {
            finish()
        }
        setSupportActionBar(toolbar)
        walletTextView.text=viewModel.getJid()
        val drawable= AcronymDrawable(viewModel.getJid()[0].toString(),1f)
        imageViewSetting.setImageDrawable(drawable)
        buttonLogout.setOnClickListener {
            viewModel.processLogout()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}