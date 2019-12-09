package com.example.balancecalculation

import java.math.BigDecimal

data class Transaction(
    var amount: BigDecimal,
    var commission : BigDecimal,
    var currency: String,
    var dateTime: String,
    var id: Int,
    var transactionStatus: String,
    var transactionType: String,
    var transactionValueId: String

)

