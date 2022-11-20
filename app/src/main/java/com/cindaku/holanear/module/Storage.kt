package com.cindaku.holanear.module


interface Storage {
    fun setString(key: String, value: String)
    fun setBoolean(key: String, value: Boolean)
    fun getString(key: String): String
    fun getBoolean(key: String): Boolean
}