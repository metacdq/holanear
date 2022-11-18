package com.cindaku.holanear.ui.item

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cindaku.holanear.R

class ChatListItem(itemView: View) : RecyclerView.ViewHolder(itemView){
    val  chatListLayout=itemView.findViewById<LinearLayout>(R.id.chatListLayout)
    val  userTextView=itemView.findViewById<TextView>(R.id.userTextView)
    val  messageTextView=itemView.findViewById<TextView>(R.id.messageTextView)
    val  dateTextView=itemView.findViewById<TextView>(R.id.dateTextView)
    val  userImage=itemView.findViewById<ImageView>(R.id.userImageView)
    val  unreadTextView=itemView.findViewById<TextView>(R.id.unreadTextView)
    val  pinImageView=itemView.findViewById<ImageView>(R.id.pinImageView)
    val  muteImageView=itemView.findViewById<ImageView>(R.id.muteImageView)
}