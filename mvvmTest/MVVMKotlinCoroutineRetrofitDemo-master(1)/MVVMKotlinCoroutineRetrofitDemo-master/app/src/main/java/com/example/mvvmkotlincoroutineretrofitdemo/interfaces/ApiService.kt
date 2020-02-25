package com.example.mvvmkotlincoroutineretrofitdemo.interfaces

import com.example.mvvmkotlincoroutineretrofitdemo.model.User
import com.example.mvvmkotlincoroutineretrofitdemo.util.Constants
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {

    @GET("/bcv/quotes/bars/btc-usd/1415404800/1416268800")
    fun getUsers(): Deferred<Response<MutableList<User>>>

}