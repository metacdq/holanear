package com.cindaku.holanear.model

enum class CallType(type: Int) {
    OUT(0),
    IN(1),
    MISS_IN(2),
    MISS_OUT(3)
}