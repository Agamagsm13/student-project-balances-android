package com.example.balancecalculation

data class Trade(
    val commission:Number,
    val commissionCurrency:String,
    val dateTime:String,
    val id:Int,
    val instrument:String,
    val tradeType:String,
    val tradeValueId:String,
    val tradedPrice:Number,
    val tradedPriceCurrency:String,
    val tradedQuantity:Number,
    val tradedQuantityCurrency:String
) {

}