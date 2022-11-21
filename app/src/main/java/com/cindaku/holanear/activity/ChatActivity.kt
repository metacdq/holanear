package com.cindaku.holanear.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.cindaku.holanear.BaseApp
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.cindaku.holanear.R
import com.cindaku.holanear.fragment.ChatListFragment
import com.cindaku.holanear.ui.adapter.MainViewPagerAdapter
import com.cindaku.holanear.ui.inf.OnChatListEvent
import com.cindaku.holanear.viewmodel.ChatMasterViewModel

class  ChatActivity : AppCompatActivity() {
    private  var onChatListEvent: OnChatListEvent?=null
    lateinit var toolbar: Toolbar
//    lateinit var viewPager2: ViewPager2
    lateinit var backImageView: ImageView
    lateinit var titleTextView: TextView
    lateinit var chatMasterViewModel: ChatMasterViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        chatMasterViewModel = ViewModelProvider(this)[ChatMasterViewModel::class.java]
        (application as BaseApp).appComponent.inject(chatMasterViewModel)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar)
        backImageView=findViewById(R.id.backImageView);
//        viewPager2=findViewById(R.id.viewPager);
        titleTextView=findViewById(R.id.titleTextView);
        backImageView.setOnClickListener {
           onChatListEvent?.clear()
        }
        backImageView.isVisible=false
//        viewPager2.adapter=MainViewPagerAdapter(this)
        titleTextView.text = getString(R.string.app_name)
//        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
//        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
//            when(position){
//                0->{
//                    tab.text = "CHAT"
//                }
//                1->{
//                    tab.text = "CALL"
//                }
//                else->{
//                    tab.text = "CHAT"
//                }
//            }
//        }.attach()
        supportFragmentManager.beginTransaction().replace(R.id.container, ChatListFragment.newInstance()).commit()
        chatMasterViewModel.init()
    }

    override fun onBackPressed() {
        if(onChatListEvent!=null && onChatListEvent!!.isSelectionMode()){
            onChatListEvent?.clear()
        }else {
            super.onBackPressed()
        }
    }
    override fun onStart() {
        super.onStart()

    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if(onChatListEvent!=null) {
            if (onChatListEvent!!.isSelectionMode()) {
                menuInflater.inflate(R.menu.chat_selected, menu)
                titleTextView.text = onChatListEvent!!.getTotalSelection().toString()
                backImageView.isVisible = true

            } else {
                menuInflater.inflate(R.menu.chat, menu)
                titleTextView.text = getString(R.string.app_name)
                backImageView.isVisible = false
            }
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        (application as BaseApp).runXMPPService()
//        (application as BaseApp).runSIPService()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menuSetting -> {
                val intent= Intent(baseContext, SettingActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.menuAddContact -> {
                val intent= Intent(baseContext, ContactActivity::class.java)
                startActivity(intent)
                true
            }
            android.R.id.home -> {
                onChatListEvent?.clear()
                true
            }
            R.id.menuDelete -> {
                onChatListEvent?.deleteChat()
                onChatListEvent?.clear()
                true
            }
            R.id.menuMute -> {
                onChatListEvent?.toggelMuted()
                onChatListEvent?.clear()
                true
            }
            R.id.menuPin -> {
                onChatListEvent?.toggelPin()
                onChatListEvent?.clear()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    fun setOnChatEvent(onChatListEvent: OnChatListEvent){
        this.onChatListEvent=onChatListEvent
    }
}