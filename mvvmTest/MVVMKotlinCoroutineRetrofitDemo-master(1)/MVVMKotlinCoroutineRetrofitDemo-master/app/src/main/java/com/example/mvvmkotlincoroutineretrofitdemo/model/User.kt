package com.example.mvvmkotlincoroutineretrofitdemo.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class User(
    @SerializedName("exchangeRate")
    @Expose
    val exchangeRate: Number,
    @SerializedName("date")
    @Expose
    val date: String
)