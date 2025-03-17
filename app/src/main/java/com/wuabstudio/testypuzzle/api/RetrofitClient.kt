package com.wuabstudio.testypuzzle.api

import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val TAG = "RetrofitClient"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val gson = GsonBuilder()
        .setLenient()
        .create()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(PixabayApiService.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
    
    val pixabayApiService: PixabayApiService by lazy {
        Log.d(TAG, "Creating PixabayApiService with base URL: ${PixabayApiService.BASE_URL}")
        retrofit.create(PixabayApiService::class.java)
    }
} 