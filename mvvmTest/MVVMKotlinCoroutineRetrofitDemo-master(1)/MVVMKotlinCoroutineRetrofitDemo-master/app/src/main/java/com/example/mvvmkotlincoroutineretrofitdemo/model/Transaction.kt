package com.example.mvvmkotlincoroutineretrofitdemo.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class Transaction(
    @SerializedName("id")
    @Expose
    val id: Int,
    @SerializedName("dateTime")
    @Expose
    val dateTime: String,
    @SerializedName("currency")
    @Expose
    val currency: String,
    @SerializedName("amount")
    @Expose
    val amount: BigDecimal,
    @SerializedName("commission")
    @Expose
    val commission: BigDecimal,
    @SerializedName("transactionStatus")
    @Expose
    val transactionStatus: String,
    @SerializedName("transactionValueId")
    @Expose
    val transactionValueId: Int
    )