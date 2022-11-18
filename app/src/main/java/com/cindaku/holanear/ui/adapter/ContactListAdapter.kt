package com.cindaku.holanear.ui.adapter

import android.annotation.SuppressLint
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

class ContactListAdapter(val context: Context,val contacts: List<Contact>,val contactChooseInterface: ContactChooseInterface): RecyclerView.Adapter<ContactListItem>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactListItem {
        val view=LayoutInflater.from(context).inflate(R.layout.item_contact,parent,false)
        return ContactListItem(view)
    }

    override fun getItemCount(): Int {
       return contacts.size
    }
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ContactListItem, position: Int) {
        holder.name.text=contacts[position].fullName
        holder.phone?.let {
            it.text="+"+contacts[position].phone
        }
        holder.layout.setOnClickListener{
            if(!contactChooseInterface.isSelectionMode()){
                contactChooseInterface.onSingle(contacts[position])
            }else{
                if(contactChooseInterface.isSelectedContact(contact = contacts[position])){
                    contactChooseInterface.onRemove(contact = contacts[position])
                }else{
                    contactChooseInterface.onAdd(contact = contacts[position])
                }
            }
        }
        holder.layout.setOnLongClickListener(object: View.OnLongClickListener{
            override fun onLongClick(p0: View?): Boolean {
                if(contactChooseInterface.isSelectedContact(contact = contacts[position])){
                    contactChooseInterface.onRemove(contact = contacts[position])
                }else{
                    contactChooseInterface.onAdd(contact = contacts[position])
                }
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
        if(contactChooseInterface.isSelectedContact(contact = contacts[position])){
            holder.layout.setBackgroundColor(context.resources.getColor(R.color.primaryTrans,null))
        }else{
            holder.layout.setBackgroundColor(context.resources.getColor(R.color.white,null))
        }
    }
    fun getData(): List<Contact>{
        return contacts
    }
}