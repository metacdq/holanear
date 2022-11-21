package com.cindaku.holanear.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.cindaku.holanear.fragment.ChatListFragment

class MainViewPagerAdapter(fa: FragmentActivity): FragmentStateAdapter(fa) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        when(position){
            0->{
                return ChatListFragment.newInstance()
            }
            else->{
                return ChatListFragment.newInstance()
            }
        }
    }
}