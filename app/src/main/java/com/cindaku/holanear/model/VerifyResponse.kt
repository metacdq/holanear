package com.cindaku.holanear.model

import android.os.Parcel
import android.os.Parcelable

class VerifyResponse() : Parcelable {
    var password: String = ""
    var jid: String = ""

    constructor(parcel: Parcel) : this() {
        password = parcel.readString().toString()
        jid = parcel.readString().toString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(password)
        parcel.writeString(jid)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VerifyResponse> {
        override fun createFromParcel(parcel: Parcel): VerifyResponse {
            return VerifyResponse(parcel)
        }

        override fun newArray(size: Int): Array<VerifyResponse?> {
            return arrayOfNulls(size)
        }
    }
}