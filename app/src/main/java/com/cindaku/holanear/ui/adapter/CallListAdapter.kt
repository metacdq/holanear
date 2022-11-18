package com.cindaku.holanear.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.cindaku.holanear.R
import com.cindaku.holanear.db.entity.CallWithDetail
import com.cindaku.holanear.extension.toReadableDuration
import com.cindaku.holanear.model.CallType
import com.cindaku.holanear.ui.drawable.AcronymDrawable
import com.cindaku.holanear.ui.item.CallListItem
import java.util.*

class CallListAdapter(val context: Context) : RecyclerView.Adapter<CallListItem>() {
    private var data: List<CallWithDetail> = listOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallListItem {
        val view=LayoutInflater.from(context).inflate(R.layout.item_call,parent,false)
        return CallListItem(view)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: CallListItem, position: Int) {
        val now= Calendar.getInstance().time.time
        if(data[position].members.size==1){
            data[position].caller.photo?.let {
                if (it != "") {
                    val options = RequestOptions()
                        .override(200,200)
                        .transform(RoundedCorners(100))
                    Glide.with(context)
                        .load(it)
                        .apply(options)
                        .fitCenter()
                        .into(holder.userImage)
                } else {
                    val drawable= AcronymDrawable(data[position].caller.fullName!![0].toString(),1f)
                    holder.userImage.setImageDrawable(drawable)
                }
                holder.userTextView.text=data[position].caller.fullName!!
            }
        }
        data[position].call.callDate?.let {
            if(now-it.time>60000){
                holder.dateTextView.text= DateUtils.getRelativeTimeSpanString(it.time)
            }else{
                val text="Just Now"
                holder.dateTextView.text=text
            }
        }
        holder.durationTextView.text=data[position].call.duration.toReadableDuration()
        if(data[position].call.type==CallType.IN){
            val drawable=context.getDrawable(R.drawable.ic_call_in)
            holder.callTypeImageView.setImageDrawable(drawable)
        }else if(data[position].call.type==CallType.OUT){
            val drawable=context.getDrawable(R.drawable.ic_call_out)
            holder.callTypeImageView.setImageDrawable(drawable)
        }else if(data[position].call.type==CallType.MISS_IN){
            val drawable=context.getDrawable(R.drawable.ic_missed_call_in)
            holder.callTypeImageView.setImageDrawable(drawable)
            holder.callTypeImageView.setImageDrawable(drawable)
        }else{
            val drawable=context.getDrawable(R.drawable.ic_missed_call_out)
            holder.callTypeImageView.setImageDrawable(drawable)
        }
    }
    fun setData(newData: List<CallWithDetail>){
        this.data=newData
        notifyDataSetChanged()
    }
    override fun getItemCount(): Int {
        return data.size
    }
}