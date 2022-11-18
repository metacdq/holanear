package com.cindaku.holanear.model

import android.os.Parcel
import android.os.Parcelable

class OTPRequest() : Parcelable{
    var phone=""
    var country_code=""
    var country_number=""

    constructor(parcel: Parcel) : this() {
        phone = parcel.readString().toString()
        country_code = parcel.readString().toString()
        country_number = parcel.readString().toString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(phone)
        parcel.writeString(country_code)
        parcel.writeString(country_number)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<OTPRequest> {
        override fun createFromParcel(parcel: Parcel): OTPRequest {
            return OTPRequest(parcel)
        }

        override fun newArray(size: Int): Array<OTPRequest?> {
            return arrayOfNulls(size)
        }
    }
}