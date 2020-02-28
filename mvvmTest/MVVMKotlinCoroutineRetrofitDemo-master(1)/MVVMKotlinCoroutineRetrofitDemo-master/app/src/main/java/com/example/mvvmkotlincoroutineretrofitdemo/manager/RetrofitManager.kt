package com.example.mvvmkotlincoroutineretrofitdemo.manager

import com.example.mvvmkotlincoroutineretrofitdemo.interfaces.ApiService
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitManager {

    val apiRate: ApiService
    init {
        val client = OkHttpClient.Builder().build()
         apiRate = Retrofit.Builder()
            .baseUrl("http://3.248.170.197:8888")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .client(client)
            .build()
            .create(ApiService::class.java)
    }

    val apiTransTrades: ApiService
    init {
        val client = OkHttpClient.Builder().build()
        apiTransTrades = Retrofit.Builder()
            .baseUrl("http://3.248.170.197:9999")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .client(client)
            .build()
            .create(ApiService::class.java)
    }




}