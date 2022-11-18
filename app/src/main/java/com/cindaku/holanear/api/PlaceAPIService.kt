package com.cindaku.holanear.api

import com.cindaku.holanear.model.NearbyPlaceResponse
import com.cindaku.holanear.model.PlaceAutocompleteResponse
import com.cindaku.holanear.model.PlaceResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PlaceAPIService {
    @GET("/maps/api/place/findplacefromtext/json")
    fun search(@Query("fields",encoded = false) fields: String,
            @Query("inputtype") inputtype: String,
            @Query("locationbias") locationbias: String,
            @Query("sensor") sensor: Boolean,
            @Query("input",encoded = true) query: String,
            @Query("key") API_KEY: String): Call<PlaceResponse>
    @GET("/maps/api/place/nearbysearch/json")
    fun autoComplete(@Query("input") keyword: String,
                     @Query("radius") radius: Int,
                     @Query("location") location: String,
               @Query("key") API_KEY: String): Call<PlaceAutocompleteResponse>
    @GET("/maps/api/place/nearbysearch/json")
    fun nearby(@Query("radius") radius: Int,
               @Query("location") location: String,
               @Query("keyword") keyword: String,
               @Query("sensor") sensor: Boolean,
               @Query("key") API_KEY: String): Call<NearbyPlaceResponse>
}