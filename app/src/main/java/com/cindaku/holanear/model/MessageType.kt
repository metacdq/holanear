package com.cindaku.holanear.model

enum class MessageType(type: Int) {
    TEXT(0),
    IMAGE(1),
    VIDEO(2),
    FILE(3),
    LOCATION(4),
    CONTACT(5),
    SYSTEM(6),
    DATE(7),
    CALL(8)
}