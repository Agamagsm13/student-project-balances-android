package com.example.mvvmkotlincoroutineretrofitdemo.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class Rate(
    @SerializedName("exchangeRate")
    @Expose
    val exchangeRate: Number,
    @SerializedName("timestamp")
    @Expose
    val date: Long
)