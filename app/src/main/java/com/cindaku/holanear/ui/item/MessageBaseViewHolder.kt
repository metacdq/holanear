package com.cindaku.holanear.ui.item

import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.cindaku.holanear.R

open class MessageBaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val layoutMessage=itemView.findViewById<LinearLayout>(R.id.layoutMessage)
    val layoutMainChat=itemView.findViewById<ConstraintLayout>(R.id.layoutMainChat)
    val layoutImage=itemView.findViewById<LinearLayout>(R.id.layoutImage)
    val layoutReply=itemView.findViewById<LinearLayout>(R.id.layoutReply)
    val layoutVideo=itemView.findViewById<ConstraintLayout>(R.id.layoutVideo)
    val layoutFile=itemView.findViewById<LinearLayout>(R.id.layoutFile)
    val textViewDateHeader=itemView.findViewById<TextView>(R.id.textViewDateHeader)
    val messageTextView=itemView.findViewById<TextView>(R.id.messageTextView)
    val dateTextView=itemView.findViewById<TextView>(R.id.dateTextView)
    val replyTextView=itemView.findViewById<TextView>(R.id.replyTextView)
    val replySubTextView=itemView.findViewById<TextView>(R.id.replySubTextView)
    val layoutContact=itemView.findViewById<LinearLayout>(R.id.layoutContact)
    val attachmentLayout=itemView.findViewById<LinearLayout>(R.id.attachmentLayout)
    val layoutLocation=itemView.findViewById<LinearLayout>(R.id.layoutLocation)
    val attachmentImageView=itemView.findViewById<ImageView>(R.id.attachmentImageView)
    val imageViewPlay=itemView.findViewById<ImageView>(R.id.imageViewPlay)
    val attachmentLocationImageView=itemView.findViewById<ImageView>(R.id.attachmentLocationImageView)
    val attachmentVideoImageView=itemView.findViewById<ImageView>(R.id.attachmentVideoImageView)
    val contactImageView=itemView.findViewById<ImageView>(R.id.contactImageView)
    val imageViewFileDownload=itemView.findViewById<ImageView>(R.id.imageViewFileDownload)
    val imageViewForward=itemView.findViewById<ImageView>(R.id.imageViewForward)
    val textViewFilename=itemView.findViewById<TextView>(R.id.textViewFilename)
    val textViewContactName=itemView.findViewById<TextView>(R.id.textViewContactName)
    val buttonAddContact=itemView.findViewById<Button>(R.id.buttonAddContact)
    val spinnerDownload=itemView.findViewById<ProgressBar>(R.id.spinnerDownload)
}