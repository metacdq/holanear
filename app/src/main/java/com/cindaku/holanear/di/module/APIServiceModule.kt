package com.cindaku.holanear.di.module

import com.cindaku.holanear.BASE_URL
import com.cindaku.holanear.api.CindakuAPIService
import com.cindaku.holanear.api.PlaceAPIService
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


@Module
class APIServiceModule {
    @Provides
    fun provideOKHTTPClient(): OkHttpClient{
        return OkHttpClient()
    }
    @Provides
    fun provideCindakuAPIService(): CindakuAPIService{
        val builder=OkHttpClient.Builder()
        builder.addInterceptor(object : Interceptor{
            override fun intercept(chain: Interceptor.Chain): Response {
                val request=chain.request()
                val addedHeader=Request.Builder()
                    .url(request.url())
                    .header("Content-Type","application/json")
                    .header("Authorization","Basic a2VudWxpbjprZW50YQ==")
                    .method(request.method(), request.body())
                    .build()
                return chain.proceed(addedHeader)
            }
        })
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(builder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(CindakuAPIService::class.java)
    }
    @Provides
    fun providePlaceAPIService(): PlaceAPIService{
        val builder=OkHttpClient.Builder()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .client(builder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(PlaceAPIService::class.java)
    }
}