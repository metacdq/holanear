package com.cindaku.holanear.di.module

import android.content.Context
import com.cindaku.holanear.APP_NAME
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferencesStorage @Inject constructor(context: Context) : Storage {
    private val sharedPreferences = context.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE)
    override fun setString(key: String, value: String) {
        with(sharedPreferences.edit()){
            putString(key,value)
            apply()
        }
    }

    override fun setBoolean(key: String, value: Boolean) {
        with(sharedPreferences.edit()){
            putBoolean(key,value)
            apply()
        }
    }

    override fun getString(key: String): String {
        return sharedPreferences.getString(key,"")!!
    }

    override fun getBoolean(key: String): Boolean {
        return sharedPreferences.getBoolean(key,false)!!
    }

}
