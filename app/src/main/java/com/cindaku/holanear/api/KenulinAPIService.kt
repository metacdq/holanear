package com.cindaku.holanear.api

import com.cindaku.holanear.model.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST


interface KenulinAPIService {
    @POST("/api/otp")
    fun otp(@Body otp: OTPRequest): Call<OTPResponse>
    @POST("/api/verify")
    fun verify(@Body verify: VerifyRequest): Call<VerifyResponse>
    @POST("/api/sync")
    fun sync(@Body sync: SyncRequest): Call<ArrayList<KenulinUser>>
}