package com.example.balancecalculation


import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.collections.ArrayList


class Model(private var allTransactions: ArrayList<Transaction>, private var allTrades: ArrayList<Trade>, private  var isDownloaded:Boolean = false, private  var years: ArrayList<Int>, private var finalBalances: MutableMap<String, BigDecimal?>){
    private var visabilityYearButton = false
    private var allAddedTransactions: ArrayList<Transaction>? = arrayListOf()                          //transactions that added artificially for positive balances
    private var idForAddTrans = 0
    fun getTransTrades(){
        if (!isDownloaded){
            val httpAsync = "http://3.248.170.197:9999/bcv/trades"
                .httpGet().responseObject<ArrayList<Trade>> { _, _, result ->
                    when (result) {
                        is Result.Failure -> {
                            val ex = result.getException()
                            println(ex)
                        }
                        is Result.Success -> {
                            allTrades = result.component1()!!
                            val httpAsync = "http://3.248.170.197:9999/bcv/transactions"
                                .httpGet().responseObject<ArrayList<Transaction>> { _, _, result ->
                                    when (result) {
                                        is Result.Failure -> {
                                            val ex = result.getException()
                                            println(ex)
                                        }
                                        is Result.Success -> {
                                            allTransactions = result.component1()!!
                                           visabilityYearButton = true
                                            if (allTrades != null) {
                                                allTrades!!.sortBy { it.dateTime }
                                                years.add(dateTimeFormatter(allTrades!!.first().dateTime).year)                      //add years from first and last trades
                                                if (!years.contains(dateTimeFormatter(allTrades!!.last().dateTime).year))
                                                    years.add(dateTimeFormatter(allTrades!!.last().dateTime).year)
                                            }
                                            if (allTransactions != null) {
                                                allTransactions!!.sortBy { it.dateTime }
                                                if (!years.contains(dateTimeFormatter(allTransactions!!.last().dateTime).year))      //add years from first and last transactions
                                                    years.add(dateTimeFormatter(allTransactions!!.last().dateTime).year)
                                                if (!years.contains(dateTimeFormatter(allTransactions!!.first().dateTime).year))
                                                    years.add(dateTimeFormatter(allTransactions!!.first().dateTime).year)
                                            }

                                            var lastDate: LocalDateTime

                                            if ((allTransactions != null) and (allTrades != null)) {                                  //searching last date
                                                lastDate = dateTimeFormatter(allTrades!!.last().dateTime)
                                                if (dateTimeFormatter(allTransactions!!.last().dateTime) > lastDate) {
                                                    lastDate = dateTimeFormatter(allTransactions!!.last().dateTime)
                                                }
                                            } else
                                                lastDate = dateTimeFormatter("2014-12-31T23:59:59")
                                            finalBalances = balanceForDateOnlyTrades(lastDate)                                        //calculation of balances after all transactions and trades
                                            for (i in (years.min()!!..years.max()!!)) {                                          //here we get sorted list of all trading years
                                                if (!years.contains(i))
                                                    years.add(i)
                                            }
                                            years.sort()
                                            isDownloaded = true

                                        }
                                    }
                                }

                            httpAsync.join()
                        }
                    }
                }

            httpAsync.join()
        }
    }
    private fun dateTimeFormatter(string: String): LocalDateTime {
        return LocalDateTime.parse(string, DateTimeFormatter.ISO_DATE_TIME)
    }

    private fun balanceForDateOnlyTrades(date: LocalDateTime): MutableMap<String, BigDecimal?> {
        val result: MutableMap<String, BigDecimal?> = mutableMapOf()
        var flagTrades = false
        var nextTrade: Trade? = null
        if (allTrades == null) {
            flagTrades = true
        } else {
            nextTrade = allTrades!![0]
        }

        var i = 1
        while (!flagTrades) {

            if (!flagTrades) {
                if (dateTimeFormatter(nextTrade!!.dateTime) > date) {
                    flagTrades = true
                } else {
                    if (!result.keys.contains(nextTrade.tradedQuantityCurrency))
                        result[nextTrade.tradedQuantityCurrency] = BigDecimal(0.0)
                    if (!result.keys.contains(nextTrade.tradedPriceCurrency))
                        result[nextTrade.tradedPriceCurrency] = BigDecimal(0.0)

                    when (nextTrade.tradeType) {
                        ("Sell") -> {
                            result[nextTrade.tradedQuantityCurrency] =
                                result[nextTrade.tradedQuantityCurrency]?.minus(nextTrade.tradedQuantity)
                            if (result[nextTrade.tradedQuantityCurrency]!! < BigDecimal(0)) {
                                allAddedTransactions?.add(
                                    Transaction(
                                        amount = -result[nextTrade.tradedQuantityCurrency]!!,
                                        commission = BigDecimal(0),
                                        currency = nextTrade.tradedQuantityCurrency,
                                        dateTime = nextTrade.dateTime,
                                        id = idForAddTrans,
                                        transactionStatus = "Complete",
                                        transactionType = "Deposit",
                                        transactionValueId = idForAddTrans.toString()
                                    )
                                )
                                idForAddTrans++
                                result[nextTrade.tradedQuantityCurrency] = BigDecimal(0)
                            }
                            result[nextTrade.tradedPriceCurrency] =
                                result[nextTrade.tradedPriceCurrency]?.plus(nextTrade.tradedPrice * nextTrade.tradedQuantity - nextTrade.commission)
                        }
                        ("Buy") -> {
                            result[nextTrade.tradedQuantityCurrency] =
                                result[nextTrade.tradedQuantityCurrency]?.plus(nextTrade.tradedQuantity - nextTrade.commission)
                            result[nextTrade.tradedPriceCurrency] =
                                result[nextTrade.tradedPriceCurrency]?.minus(nextTrade.tradedPrice * nextTrade.tradedQuantity)
                            if (result[nextTrade.tradedPriceCurrency]!! < BigDecimal(0)) {
                                allAddedTransactions?.add(
                                    Transaction(
                                        amount = -result[nextTrade.tradedPriceCurrency]!!,
                                        commission = BigDecimal(0),
                                        currency = nextTrade.tradedPriceCurrency,
                                        dateTime = nextTrade.dateTime,
                                        id = idForAddTrans,
                                        transactionStatus = "Complete",
                                        transactionType = "Deposit",
                                        transactionValueId = idForAddTrans.toString()
                                    )
                                )
                                idForAddTrans++
                                result[nextTrade.tradedPriceCurrency] = BigDecimal(0)
                            }
                        }
                    }
                }
            }

            if (!flagTrades) {
                if (allTrades!!.size == i) {
                    flagTrades = true
                } else {
                    nextTrade = allTrades!![i]
                }
            }

            i++
        }
        return result
    }


}
