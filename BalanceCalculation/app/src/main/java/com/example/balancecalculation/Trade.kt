package com.example.balancecalculation

import java.math.BigDecimal

data class Trade(
    val commission:BigDecimal,
    val commissionCurrency:String,
    val dateTime:String,
    val id:Int,
    val instrument:String,
    val tradeType:String,
    val tradeValueId:String,
    val tradedPrice:BigDecimal,
    val tradedPriceCurrency:String,
    val tradedQuantity:BigDecimal,
    val tradedQuantityCurrency:String
)