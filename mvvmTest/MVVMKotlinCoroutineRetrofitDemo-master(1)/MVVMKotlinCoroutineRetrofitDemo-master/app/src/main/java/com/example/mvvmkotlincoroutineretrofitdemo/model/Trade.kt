package com.example.mvvmkotlincoroutineretrofitdemo.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class Trade(
    @SerializedName("id")
    @Expose
    val id: Int,
    @SerializedName("dateTime")
    @Expose
    val dateTime: String,
    @SerializedName("instrument")
    @Expose
    val instrument: String,
    @SerializedName("tradeType")
    @Expose
    val tradeType: String,
    @SerializedName("tradedQuantity")
    @Expose
    val tradedQuantity: BigDecimal,
    @SerializedName("tradedQuantityCurrency")
    @Expose
    val tradedQuantityCurrency: String,
    @SerializedName("tradedPrice")
    @Expose
    val tradedPrice: BigDecimal,
    @SerializedName("tradedPriceCurrency")
    @Expose
    val tradedPriceCurrency: String,
    @SerializedName("commission")
    @Expose
    val commission: BigDecimal,
    @SerializedName("commissionCurrency")
    @Expose
    val commissionCurrency: String,
    @SerializedName("tradeValueId")
    @Expose
    val tradeValueId: Int
)