package com.cindaku.holanear.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.cindaku.holanear.di.module.ChatRepository
import com.cindaku.holanear.di.module.XMPPConnector
import javax.inject.Inject

class ImagePreviewViewModel: ViewModel() {
    @Inject
    lateinit var xmppConnector: XMPPConnector
    @Inject
    lateinit var chatRepository: ChatRepository
    val images: ArrayList<HashMap<String,String>> = arrayListOf()
    var activeIndex=0
    fun load(arrayList: ArrayList<HashMap<String,String>>){
        images.addAll(arrayList)
    }
    fun reload(arrayList: ArrayList<HashMap<String,String>>){
        images.clear()
        images.addAll(arrayList)
    }
    fun add(data: HashMap<String,String>){
        images.add(data)
    }
    fun imagesToString(): ArrayList<String>{
        val list= arrayListOf<String>()
        images.forEach {
            it["image"]?.let { image->
                list.add(image)
            }
        }
        return list
    }
}