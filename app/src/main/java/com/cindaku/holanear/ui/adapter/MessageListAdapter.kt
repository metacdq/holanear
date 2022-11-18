package com.cindaku.holanear.ui.adapter

import android.annotation.SuppressLint
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
import com.google.gson.Gson
import com.cindaku.holanear.R
import com.cindaku.holanear.db.entity.ChatMessage
import com.cindaku.holanear.model.ContactMessage
import com.cindaku.holanear.model.LocationMessage
import com.cindaku.holanear.model.MessageType
import com.cindaku.holanear.model.ReplySnippet
import com.cindaku.holanear.ui.drawable.AcronymDrawable
import com.cindaku.holanear.ui.inf.OnMessageEvent
import com.cindaku.holanear.ui.inf.OnSearchMessageResult
import com.cindaku.holanear.ui.item.MessageBaseViewHolder
import com.cindaku.holanear.ui.item.MessageLeftItem
import com.cindaku.holanear.ui.item.MessageRightItem
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class MessageListAdapter(val context: Context) :
    RecyclerView.Adapter<MessageBaseViewHolder>(), OnSearchMessageResult {
    private var messages: List<ChatMessage> = listOf()
    private var searchResults: List<ChatMessage> = listOf()
    private var onMessageEvent: OnMessageEvent?=null
    private var currentPosition=0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageBaseViewHolder {
        if(viewType==0){
            val view=LayoutInflater.from(context).inflate(R.layout.item_message_left,parent,false)
            return MessageLeftItem(view)
        }else{
            val view=LayoutInflater.from(context).inflate(R.layout.item_message_right,parent,false)
            return MessageRightItem(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if(messages[position].isLeft){
            return 0
        }
        return 1
    }
    override fun getItemCount(): Int {
        return messages.size
    }

    @SuppressLint("UseCompatLoadingForDrawables", "SimpleDateFormat")
    override fun onBindViewHolder(holder: MessageBaseViewHolder, position: Int) {
        if(!messages[position].isDeleted) {
            val now = Calendar.getInstance().time.time
            messages[position].messageType?.let {
                try {
                    if (it == MessageType.DATE) {
                        holder.layoutMainChat.isVisible = false
                        holder.textViewDateHeader.isVisible = true
                    } else {
                        holder.layoutMainChat.isVisible = true
                        holder.textViewDateHeader.isVisible = false
                    }
                    holder.layoutFile.isVisible = false
                    holder.layoutImage.isVisible = false
                    holder.layoutContact.isVisible = false
                    holder.layoutLocation.isVisible = false
                    holder.layoutVideo.isVisible = false
                    if (it == MessageType.IMAGE) {
                        holder.layoutImage.isVisible = true
                        holder.attachmentLayout.isVisible = true
                        val detailMessage = Gson().fromJson(
                            messages[position].body!!,
                            HashMap<String, String>()::class.java
                        )
                        if (messages[position].attachment == "") {
                            val options = RequestOptions()
                                .override(400, 400)
                                .transform(RoundedCorners(10))
                            Glide.with(context)
                                .load(detailMessage["url"]!!)
                                .apply(options)
                                .centerCrop()
                                .into(holder.attachmentImageView)
                        } else {
                            val options = RequestOptions()
                                .override(400, 400)
                                .transform(RoundedCorners(10))
                            Glide.with(context)
                                .load(File(messages[position].attachment!!))
                                .apply(options)
                                .centerCrop()
                                .into(holder.attachmentImageView)
                        }
                        holder.messageTextView.isVisible = true
                        holder.messageTextView.text = detailMessage["caption"]!!
                        holder.attachmentImageView.setOnClickListener {
                            onMessageEvent?.let {
                                if (!it.isSelectedMode()) {
                                    it.onImageViewerRequest(messages[position])
                                } else {
                                    if (it.checkSelected(message = messages[position])) {
                                        it.unSelectMessage(message = messages[position])
                                    } else {
                                        it.selectMessage(message = messages[position])
                                    }
                                }
                            }
                        }
                    } else if (it == MessageType.VIDEO) {
                        holder.attachmentLayout.isVisible = true
                        holder.layoutVideo.isVisible = true
                        val detailMessage = Gson().fromJson(
                            messages[position].body!!,
                            HashMap<String, String>()::class.java
                        )
                        if (messages[position].attachment == "") {
                            val thumb: Long = 1000
                            val options = RequestOptions()
                                .frame(thumb)
                                .override(400, 400)
                                .transform(RoundedCorners(10))
                            Glide.with(context)
                                .load(detailMessage["url"]!!)
                                .apply(options)
                                .centerCrop()
                                .into(holder.attachmentVideoImageView)
                        } else {
                            val thumb: Long = 1000
                            val options = RequestOptions()
                                .frame(thumb)
                                .override(400, 400)
                                .transform(RoundedCorners(10))
                            Glide.with(context)
                                .load(File(messages[position].attachment!!))
                                .apply(options)
                                .centerCrop()
                                .into(holder.attachmentVideoImageView)
                        }
                        holder.messageTextView.isVisible = false
                        holder.imageViewPlay.setOnClickListener {
                            onMessageEvent?.let {
                                if (!it.isSelectedMode()) {
                                    it.onVideoPlayRequest(messages[position])
                                } else {
                                    if (it.checkSelected(message = messages[position])) {
                                        it.unSelectMessage(message = messages[position])
                                    } else {
                                        it.selectMessage(message = messages[position])
                                    }
                                }
                            }
                        }
                    } else if (it == MessageType.DATE) {
                        val format = SimpleDateFormat("dd-MM-yyyy")
                        val dateEnd = format.format(now)
                        val dateStart = format.format(messages[position].sentDate!!.time)
                        val diff = format.parse(dateEnd)!!.time - format.parse(dateStart)!!.time
                        if (diff < 86400000) {
                            val text = "Today"
                            holder.textViewDateHeader.text = text
                        } else if (diff >= 86400000 && diff < 172800000) {
                            val text = "Yesterday"
                            holder.textViewDateHeader.text = text
                        } else {
                            holder.textViewDateHeader.text = messages[position].body
                        }
                    } else if (it == MessageType.FILE) {
                        holder.attachmentLayout.isVisible = true
                        holder.layoutFile.isVisible = true
                        holder.textViewFilename.text = messages[position].attachmentName
                        holder.messageTextView.isVisible = false
                        holder.imageViewFileDownload.setOnClickListener {
                            onMessageEvent?.let {
                                if (!it.isSelectedMode()) {
                                    holder.spinnerDownload.isVisible = true
                                    it.onDownloadRequest(messages[position])
                                } else {
                                    if (it.checkSelected(message = messages[position])) {
                                        it.unSelectMessage(message = messages[position])
                                    } else {
                                        it.selectMessage(message = messages[position])
                                    }
                                }
                            }
                        }
                        holder.imageViewFileDownload.isVisible =
                            messages[position].attachment.equals("")
                    } else if (it == MessageType.CONTACT) {
                        val contact = Gson().fromJson<ContactMessage>(
                            messages[position].body,
                            ContactMessage::class.java
                        )
                        holder.attachmentLayout.isVisible = true
                        holder.layoutContact.isVisible = true
                        holder.textViewContactName.text = contact.name
                        holder.contactImageView.setImageDrawable(
                            AcronymDrawable(
                                contact.name[0].toString(),
                                1f
                            )
                        )
                        holder.buttonAddContact.setOnClickListener {
                            onMessageEvent?.let {
                                if (!it.isSelectedMode()) {
                                    it.requestAddContact(contact)
                                } else {
                                    if (it.checkSelected(message = messages[position])) {
                                        it.unSelectMessage(message = messages[position])
                                    } else {
                                        it.selectMessage(message = messages[position])
                                    }
                                }
                            }
                        }
                        holder.messageTextView.isVisible = false
                        onMessageEvent?.let {
                            val exists=it.checkContact(contact.phones[0])
                            if(exists.isNotEmpty() == false) {
                                holder.buttonAddContact.isVisible = false
                            }else{
                                holder.buttonAddContact.isVisible = false
                                holder.layoutContact.setOnClickListener { view->
                                    it.goToContact(exists[0])
                                }
                            }
                        }
                    } else if (it == MessageType.LOCATION) {
                        holder.layoutLocation.isVisible = true
                        holder.attachmentLayout.isVisible = true
                        val detailMessage =
                            Gson().fromJson(messages[position].body!!, LocationMessage::class.java)
                        if (messages[position].attachment == "") {
                            val options = RequestOptions()
                                .override(400, 400)
                                .transform(RoundedCorners(10))
                            Glide.with(context)
                                .load(detailMessage.url)
                                .apply(options)
                                .centerCrop()
                                .into(holder.attachmentLocationImageView)
                        } else {
                            val options = RequestOptions()
                                .override(400, 400)
                                .transform(RoundedCorners(10))
                            Glide.with(context)
                                .load(File(messages[position].attachment!!))
                                .apply(options)
                                .centerCrop()
                                .into(holder.attachmentLocationImageView)
                        }
                        holder.attachmentLocationImageView.setOnClickListener {
                            onMessageEvent?.let {
                                if (!it.isSelectedMode()) {
                                    it.onGeoNavigate(detailMessage)
                                } else {
                                    if (it.checkSelected(message = messages[position])) {
                                        it.unSelectMessage(message = messages[position])
                                    } else {
                                        it.selectMessage(message = messages[position])
                                    }
                                }
                            }
                        }
                        holder.messageTextView.isVisible = true
                        holder.messageTextView.text = messages[position].attachmentName!!
                    } else {
                        holder.attachmentLayout.isVisible = false
                        messages[position].body?.let {
                            holder.messageTextView.isVisible = true
                            holder.messageTextView.text = it
                        }
                    }
                    messages[position].isLoading.let {
                        holder.spinnerDownload.isVisible = it
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            messages[position].sentDate?.let {
                holder.dateTextView.isVisible=true
                if (now - it.time > 60000) {
                    holder.dateTextView.text = DateUtils.getRelativeTimeSpanString(it.time)
                } else {
                    val text = "Just Now"
                    holder.dateTextView.text = text
                }
            }
            holder.layoutReply.isVisible=false
            onMessageEvent?.let {
                if(!messages[position].replyBody.isNullOrEmpty()){
                    val body=Gson().fromJson<ReplySnippet>(messages[position].replyBody,ReplySnippet::class.java)
                    if(body!=null){
                        holder.layoutReply.isVisible=true
                        holder.replyTextView.text=it.getReplyContactName(body.senderJid)
                        holder.replySubTextView.text= body.snippetText
                        holder.layoutReply.setOnClickListener { view->
                            it.onRequestScrollToMessageId(body.messageId)
                        }
                    }
                }
            }
            messages[position].isLeft.let {
                if (!it) {
                    val rightHolder = holder as MessageRightItem
                    rightHolder.imageViewTick.isVisible=true
                    if (messages[position].isSent && messages[position].isReaded) {
                        rightHolder.imageViewTick.setImageDrawable(
                            context.resources.getDrawable(R.drawable.ic_tick_double_blue, null)
                        )
                    } else if (messages[position].isSent) {
                        rightHolder.imageViewTick.setImageDrawable(
                            context.resources.getDrawable(R.drawable.ic_double_tick, null)
                        )
                    } else {
                        rightHolder.imageViewTick.setImageDrawable(
                            context.resources.getDrawable(R.drawable.ic_tick, null)
                        )
                    }
                }
            }
            holder.layoutMainChat.setOnLongClickListener(object : View.OnLongClickListener {
                override fun onLongClick(p0: View?): Boolean {
                    onMessageEvent?.let {
                        clearSearch()
                        it.onLongPressed(message = messages[position])
                    }
                    return true
                }
            })
            holder.layoutMainChat.setOnClickListener {
                onMessageEvent?.let {
                    if (!it.isSelectedMode()) {
                        it.onClick(message = messages[position])
                    } else {
                        if (it.checkSelected(message = messages[position])) {
                            it.unSelectMessage(message = messages[position])
                        } else {
                            it.selectMessage(message = messages[position])
                        }
                    }
                }
            }
            if (searchResults.isEmpty()) {
                onMessageEvent?.let {
                    if (it.checkSelected(message = messages[position])) {
                        holder.layoutMessage.setBackgroundColor(
                            context.resources.getColor(
                                R.color.blackTransChat,
                                null
                            )
                        )
                    } else {
                        holder.layoutMessage.setBackgroundColor(
                            context.resources.getColor(
                                android.R.color.transparent,
                                null
                            )
                        )
                    }
                }
            } else {
                if (searchResults.indexOf(messages[position]) >= 0) {
                    holder.layoutMessage.setBackgroundColor(
                        context.resources.getColor(
                            R.color.blackTransChat,
                            null
                        )
                    )
                } else {
                    holder.layoutMessage.setBackgroundColor(
                        context.resources.getColor(
                            android.R.color.transparent,
                            null
                        )
                    )
                }
            }
            holder.imageViewForward.isVisible=messages[position].isForwarded
        }else{
            //Deleted Message
            holder.imageViewForward.isVisible=false
            holder.layoutMainChat.isVisible = true
            holder.attachmentLayout.isVisible = false
            holder.messageTextView.isVisible = true
            holder.textViewDateHeader.isVisible = false
            holder.spinnerDownload.isVisible = false
            holder.messageTextView.text="This message was deleted"
            holder.replyTextView.isVisible=false
            holder.dateTextView.isVisible=false
            if(!messages[position].isLeft){
                val rightHolder = holder as MessageRightItem
                rightHolder.imageViewTick.isVisible=false
            }
            holder.layoutMessage.setBackgroundColor(
                context.resources.getColor(
                    android.R.color.transparent,
                    null
                )
            )
        }
    }
    fun setOnMessage(onMesage: OnMessageEvent){
        onMessageEvent=onMesage
    }
    fun setData(data: List<ChatMessage>){
        currentPosition=0
        messages=data
        notifyDataSetChanged()
    }

    override fun onResult(results: List<ChatMessage>) {
        currentPosition=0
        searchResults=results
        notifyDataSetChanged()
    }

    override fun getNext(): Int? {
        if(searchResults.size<1){
            return null
        }
        if(currentPosition<searchResults.size-1){
            currentPosition=currentPosition+1
        }
        val item=searchResults[currentPosition]
        return messages.indexOf(item)
    }

    override fun getPrevious(): Int? {
        if(searchResults.size<1){
            return null
        }
        if(currentPosition>0){
            currentPosition=currentPosition-1
        }
        val item=searchResults[currentPosition]
        return messages.indexOf(item)
    }

    override fun clearSearch() {
        searchResults= listOf()
        notifyDataSetChanged()
    }
    fun findMessageOnList(chatMessage: ChatMessage): Int{
        return messages.indexOf(chatMessage)
    }
}