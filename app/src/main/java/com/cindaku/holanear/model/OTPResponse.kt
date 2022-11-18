package com.cindaku.holanear.model

import android.os.Parcel
import android.os.Parcelable

class OTPResponse() : Parcelable {
    var phone: String=""
    var email: String=""
    var jid: String=""

    constructor(parcel: Parcel) : this() {
        phone = parcel.readString().toString()
        email = parcel.readString().toString()
        jid = parcel.readString().toString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(phone)
        parcel.writeString(email)
        parcel.writeString(jid)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<OTPResponse> {
        override fun createFromParcel(parcel: Parcel): OTPResponse {
            return OTPResponse(parcel)
        }

        override fun newArray(size: Int): Array<OTPResponse?> {
            return arrayOfNulls(size)
        }
    }
}