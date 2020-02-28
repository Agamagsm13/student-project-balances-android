package com.example.mvvmkotlincoroutineretrofitdemo.interfaces

import com.example.mvvmkotlincoroutineretrofitdemo.model.Rate
import com.example.mvvmkotlincoroutineretrofitdemo.model.Trade
import com.example.mvvmkotlincoroutineretrofitdemo.model.Transaction
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {

    @GET("/bcv/quotes/bars/btc-usd/1579960500/1582588500")
    fun getRates(): Deferred<Response<MutableList<Rate>>>
    @GET("/bcv/transactions")
    fun getTrans(): Deferred<Response<MutableList<Transaction>>>
    @GET("/bcv/trades")
    fun getTrades(): Deferred<Response<MutableList<Trade>>>

}