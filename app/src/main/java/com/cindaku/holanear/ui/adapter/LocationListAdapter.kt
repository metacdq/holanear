package com.cindaku.holanear.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cindaku.holanear.R
import com.cindaku.holanear.model.Place
import com.cindaku.holanear.ui.inf.OnChooseLocation
import com.cindaku.holanear.ui.item.LocationItem
import java.util.ArrayList

class LocationListAdapter(val context: Context): RecyclerView.Adapter<LocationItem>() {
    private var data= arrayListOf<Place>()
    private var onChoose: OnChooseLocation?=null
    private var choosed: Place?=null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationItem {
        val view=LayoutInflater.from(context).inflate(R.layout.item_location,parent,false)
        return LocationItem(view)
    }

    override fun onBindViewHolder(holder: LocationItem, position: Int) {
        if(data[position].equals(choosed)){
            holder.layoutLocation.setBackgroundColor(context.resources.getColor(R.color.primaryTrans,null))
        }else{
            holder.layoutLocation.setBackgroundColor(context.resources.getColor(R.color.white,null))
        }
        holder.textViewLocationName.text=data[position].name
        holder.textViewLocationAddress.text=data[position].formatted_address
        holder.layoutLocation.setOnClickListener {
            onChoose?.let {
                choosed=data[position]
                notifyDataSetChanged()
                it.onChoose(choosed!!)
            }
        }
    }
    fun setOnChoose(choose: OnChooseLocation){
        onChoose=choose
    }
    fun setData(newData: ArrayList<Place>){
        data=newData
        notifyDataSetChanged()
    }
    override fun getItemCount(): Int {
        return data.size
    }
}