package com.example.mvvmkotlincoroutineretrofitdemo.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class User(
    @SerializedName("id")
    @Expose
    val id: BigDecimal,
    @SerializedName("name")
    @Expose
    val name: Long
)