package com.cindaku.holanear.ui.item

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.cindaku.holanear.R

class ImagePreviewItem(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val image=itemView.findViewById<ImageView>(R.id.imageView)
}