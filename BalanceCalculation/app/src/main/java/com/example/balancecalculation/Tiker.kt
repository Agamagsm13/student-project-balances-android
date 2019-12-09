package com.example.balancecalculation

import java.math.BigDecimal

data class Tiker(
    val avg:BigDecimal,
    val buy:BigDecimal,
    val high:BigDecimal,
    val last:BigDecimal,
    val low:BigDecimal,
    val sell:BigDecimal,
    val vol:BigDecimal,
    val vol_cur:BigDecimal,
    val pair:String,
    val updated:Int
)