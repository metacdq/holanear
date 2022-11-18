package com.cindaku.holanear.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cindaku.holanear.R
import com.cindaku.holanear.activity.ImagePreviewActivity
import com.cindaku.holanear.ui.item.ImagePreviewItem
import java.io.File
import java.lang.Exception

class ImagePreviewAdapter(val  activity: ImagePreviewActivity,
                          val items: ArrayList<HashMap<String,String>>): RecyclerView.Adapter<ImagePreviewItem>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagePreviewItem {
        val view=LayoutInflater.from(activity).inflate(R.layout.item_image_preview,parent,false)
        return ImagePreviewItem((view))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ImagePreviewItem, position: Int) {
       try{
           val data=items.get(position)
           data["image"]?.let {
               val options = RequestOptions()
                   .override(200,200)
               Glide.with(activity)
                   .load(File(it))
                   .apply(options)
                   .fitCenter()
                   .into(holder.image)
               holder.image.setOnClickListener {
                   activity.setActiveImage(position)
               }
           }
       }catch (e: Exception){
           e.printStackTrace()
       }
    }
}