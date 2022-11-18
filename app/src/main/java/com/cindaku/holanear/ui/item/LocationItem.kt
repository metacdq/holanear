package com.cindaku.holanear.ui.item

import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.cindaku.holanear.R

class LocationItem(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val layoutLocation=itemView.findViewById<ConstraintLayout>(R.id.layoutLocation)
    val textViewLocationName=itemView.findViewById<TextView>(R.id.textViewLocationName)
    val textViewLocationAddress=itemView.findViewById<TextView>(R.id.textViewLocationAddress)
}