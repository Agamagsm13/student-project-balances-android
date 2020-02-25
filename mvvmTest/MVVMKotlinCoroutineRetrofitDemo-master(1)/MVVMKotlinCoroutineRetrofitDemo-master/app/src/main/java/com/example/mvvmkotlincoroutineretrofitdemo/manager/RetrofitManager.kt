package com.example.mvvmkotlincoroutineretrofitdemo.manager

import com.example.mvvmkotlincoroutineretrofitdemo.interfaces.ApiService
import com.example.mvvmkotlincoroutineretrofitdemo.util.Constants
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.CallAdapter.Factory

object RetrofitManager {

    val apiService: ApiService

    init {

        val client = OkHttpClient.Builder().build()

     //   apiService = Retrofit.Builder()
     //       .baseUrl(Constants.BASE_URL)
     //       .addConverterFactory(GsonConverterFactory.create())
     //       .client(client)
     //       .build()
     //       .create(ApiService::class.java)

         apiService = Retrofit.Builder()
            .baseUrl("http://3.248.170.197:8888")
             .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .client(client)
            .build()
            .create(ApiService::class.java)
    }


}