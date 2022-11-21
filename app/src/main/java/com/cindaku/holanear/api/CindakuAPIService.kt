package com.cindaku.holanear.api

import com.cindaku.holanear.model.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST


interface CindakuAPIService {
    @POST("/api/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>
}