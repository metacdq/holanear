package com.cindaku.holanear.ui.item

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cindaku.holanear.R

class CallListItem(view: View) : RecyclerView.ViewHolder(view) {
    val  callListLayout=itemView.findViewById<LinearLayout>(R.id.callListLayout)
    val  userTextView=itemView.findViewById<TextView>(R.id.userTextView)
    val  durationTextView=itemView.findViewById<TextView>(R.id.durationTextView)
    val  dateTextView=itemView.findViewById<TextView>(R.id.dateTextView)
    val  userImage=itemView.findViewById<ImageView>(R.id.userImageView)
    val  callTypeImageView=itemView.findViewById<ImageView>(R.id.callTypeImageView)
}