package com.cindaku.holanear.model

enum class SystemMesageType(type: Int) {
    DELETE_MESSAGE(0),
    INIT_ANONYMOUS(1),
    MISS_CALL(2),
    JOIN_ROOM(3),
    LEAVE_ROOM(4),
    CHANGE_PROFILE_GROUP(5),
    INFO(6)
}