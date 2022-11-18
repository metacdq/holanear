package com.cindaku.holanear.utils

import com.cindaku.holanear.db.entity.ChatMessage
import java.math.BigInteger
import java.security.MessageDigest

class IDUtils {
    companion object{
        fun generateMessageStanzaId(chatMessage: ChatMessage): String{
            val id=""+chatMessage.sentDate!!.time+""+chatMessage.messageType!!+chatMessage.sender!!
            val md5=MessageDigest.getInstance("MD5")
            val bigInt = BigInteger(1, md5.digest(id.toByteArray(Charsets.UTF_8)))
            return String.format("%032x", bigInt)
        }
    }
}