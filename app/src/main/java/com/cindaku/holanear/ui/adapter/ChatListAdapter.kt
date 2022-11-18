package com.cindaku.holanear.ui.adapter

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.cindaku.holanear.R
import com.cindaku.holanear.db.entity.ChatListWithDetail
import com.cindaku.holanear.model.ChatType
import com.cindaku.holanear.model.MessageType
import com.cindaku.holanear.ui.drawable.AcronymDrawable
import com.cindaku.holanear.ui.inf.OnChatListEvent
import com.cindaku.holanear.ui.item.ChatListItem
import java.util.*


class ChatListAdapter(val context: Context): RecyclerView.Adapter<ChatListItem>() {
    private var onChatListEvent: OnChatListEvent?=null
    private var list: List<ChatListWithDetail> = listOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListItem {
        val view=LayoutInflater.from(context).inflate(R.layout.item_chat,parent,false)
        return ChatListItem(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ChatListItem, position: Int) {
        try{
            val now= Calendar.getInstance().time.time
            if(list[position].chatList.chatType==ChatType.PERSONAL) {
                list[position].contact?.let {
                    if (it.photo != "") {
                        val options = RequestOptions()
                            .override(200,200)
                            .transform(RoundedCorners(100))
                        Glide.with(context)
                            .load(it.photo)
                            .apply(options)
                            .fitCenter()
                            .into(holder.userImage)
                    } else {
                        val drawable= AcronymDrawable(it.fullName!![0].toString(),1f)
                        holder.userImage.setImageDrawable(drawable)
                    }
                    holder.userTextView.text = it.fullName
                }
                holder.chatListLayout.setOnClickListener {
                    onChatListEvent?.let {
                        if(!it.isSelectionMode()){
                            it.onClickItem(list[position])
                        }else{
                            if(it.isSelected(list[position])){
                                it.onRemove(list[position])
                            }else{
                                it.onAdd(list[position])
                            }
                        }
                    }
                }
                holder.chatListLayout.setOnLongClickListener(object : View.OnLongClickListener{
                    override fun onLongClick(p0: View?): Boolean {
                        onChatListEvent?.let {
                            if(it.isSelected(list[position])){
                                it.onRemove(list[position])
                            }else{
                                it.onAdd(list[position])
                            }
                        }
                        return true
                    }

                })
                onChatListEvent?.let {
                    if(it.isSelected(list[position])){
                        holder.chatListLayout.setBackgroundColor(context.resources.getColor(R.color.primaryTrans,null))
                    }else{
                        holder.chatListLayout.setBackgroundColor(context.resources.getColor(R.color.white,null))
                    }
                }
                list[position].lastMessage?.let { message->
                     var lastMessage=""
                     if(!message.isDeleted){
                         if(message.isLeft){
                             lastMessage= list[position].contact!!.fullName.toString()
                         }else{
                             lastMessage="You"
                         }
                         if(message.messageType== MessageType.TEXT){
                             lastMessage=lastMessage+": "+message.body!!
                         }else if(message.messageType== MessageType.IMAGE){
                             lastMessage=lastMessage+": "+"Send An Image"
                         }else if (message.messageType== MessageType.VIDEO){
                             lastMessage=lastMessage+": "+"Send An Video"
                         }else if (message.messageType== MessageType.FILE){
                             lastMessage=lastMessage+": "+"Send An File"
                         }else if (message.messageType== MessageType.CONTACT){
                             lastMessage=lastMessage+": "+"Send An Contact"
                         }else if (message.messageType== MessageType.LOCATION){
                             lastMessage="Share Location"
                         }else{
                             lastMessage=lastMessage+": "+"Send Message"
                         }
                     }else{
                         if(message.isLeft){
                             lastMessage= list[position].contact!!.fullName.toString()+
                                     ": This Message was deleted"
                         }else{
                             lastMessage="You: This Message was deleted"
                         }
                     }
                     holder.messageTextView.text=lastMessage
                }
                list[position].chatList.lastUpdate?.let {
                    if(now-it.time>60000){
                        holder.dateTextView.text=DateUtils.getRelativeTimeSpanString(it.time)
                    }else{
                        val text="Just Now"
                        holder.dateTextView.text=text
                    }
                }
                holder.muteImageView.isVisible=list[position].chatList.isMuted
                holder.pinImageView.isVisible=list[position].chatList.isPinned
                if(list[position].unread>0){
                    holder.unreadTextView.isVisible=true
                    holder.unreadTextView.text=list[position].unread.toString()
                }else{
                    holder.unreadTextView.isVisible=false
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
        }

    }
    fun setInf(onChatListEvent: OnChatListEvent){
        this.onChatListEvent=onChatListEvent
    }
    fun setData(data: List<ChatListWithDetail>){
        list=data
        notifyDataSetChanged()
    }
}