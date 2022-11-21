package com.cindaku.holanear.model

class LoginResponse {
    var status = false
    var data: LoginData? = null
}
class LoginData{
    var xmpp_token = ""
}