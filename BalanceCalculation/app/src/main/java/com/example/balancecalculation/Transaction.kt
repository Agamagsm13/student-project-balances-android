package com.example.balancecalculation

import java.time.temporal.TemporalAmount
import java.util.*

enum class Status{
    Complete, NotComlete
}
enum class Curr{
    BTC, USD, RUB
}
enum class Type{
    Deposit, Withdraw
}
class Transaction(
    date: String,
    typeTransaction : Type,
    curr: Curr,
    amountTransaction : Double,
    commissionTransaction : Float,
    statusTransaction: Status,
    idTransaction : Int
) {
    val dataTime = date
    val type = typeTransaction
    val currency = curr
    val amount = amountTransaction
    val commission = commissionTransaction
    val status = statusTransaction
    val id = idTransaction


}