package com.cindaku.holanear.ui.item

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cindaku.holanear.R

class ContactListItem(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val image=itemView.findViewById<ImageView>(R.id.photoImageView)
    val name=itemView.findViewById<TextView>(R.id.nameTextview)
    val layout=itemView.findViewById<LinearLayout>(R.id.contactLayout)
    val phone: TextView?=itemView.findViewById(R.id.phoneTextview)
}