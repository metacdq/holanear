package com.cindaku.holanear.model

import android.os.Parcel
import android.os.Parcelable

class Location(): Parcelable {
    var lat: Double= 0.0
    var lng: Double=0.0
    var description=""

    constructor(parcel: Parcel) : this() {
        lat = parcel.readDouble()
        lng = parcel.readDouble()
        description = parcel.readString().toString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(lat)
        parcel.writeDouble(lng)
        parcel.writeString(description)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Location> {
        override fun createFromParcel(parcel: Parcel): Location {
            return Location(parcel)
        }

        override fun newArray(size: Int): Array<Location?> {
            return arrayOfNulls(size)
        }
    }

}