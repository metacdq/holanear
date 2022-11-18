package com.cindaku.holanear.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.cindaku.holanear.R
import com.cindaku.holanear.db.entity.Contact
import com.cindaku.holanear.ui.drawable.AcronymDrawable
import com.cindaku.holanear.ui.inf.ContactChooseInterface
import com.cindaku.holanear.ui.item.ContactListItem

class SelectedContactListAdapter(val context: Context,val contactChooseInterface: ContactChooseInterface): RecyclerView.Adapter<ContactListItem>() {
    private var contacts: List<Contact> = listOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactListItem {
        val view=LayoutInflater.from(context).inflate(R.layout.item_selected_contact,parent,false)
        return ContactListItem(view)
    }

    override fun getItemCount(): Int {
        return contacts.size
    }
    override fun onBindViewHolder(holder: ContactListItem, position: Int) {
        holder.name.text=contacts[position].fullName
        holder.layout.setOnClickListener{
            contactChooseInterface.onRemove(contact = contacts[position])
        }
        holder.layout.setOnLongClickListener(object: View.OnLongClickListener{
            override fun onLongClick(p0: View?): Boolean {
                contactChooseInterface.onRemove(contact = contacts[position])
                return true
            }
        })
        if(contacts[position].photo!="") {
            val options = RequestOptions()
                .override(200,200)
                .transform(RoundedCorners(100))
            Glide.with(context)
                .load(contacts[position].photo)
                .apply(options)
                .fitCenter()
                .into(holder.image)
        }else{
            val drawable= AcronymDrawable(contacts[position].fullName!![0].toString(),1f)
            holder.image.setImageDrawable(drawable)
        }
    }
    fun setSelected(data: List<Contact>){
        contacts=data
        notifyDataSetChanged()
    }
}