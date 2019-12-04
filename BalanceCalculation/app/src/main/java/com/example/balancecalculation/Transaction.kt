package com.example.balancecalculation

data class Transaction(
    var amount: Double,
    var commission : Double,
    var currency: String,
    var dateTime: String,
    var id: Int,
    var transactionStatus: String,
    var transactionType: String,
    var transactionValueId: String

)

