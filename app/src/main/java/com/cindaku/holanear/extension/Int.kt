package com.cindaku.holanear.extension

fun Int.toReadableDuration(): String{
    var str=""
    if(this>3600){
        str=(this%3600).toString()+" hours "
    }
    if(this>60){
        str=str+(this%60).toString()+" minutes "
    }
    str=str+((this%3600)%60).toString()+" seconds"
    return str
}