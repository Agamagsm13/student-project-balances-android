package com.example.mvvmkotlincoroutineretrofitdemo.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.mvvmkotlincoroutineretrofitdemo.manager.RetrofitManager
import com.example.mvvmkotlincoroutineretrofitdemo.model.Rate
import com.example.mvvmkotlincoroutineretrofitdemo.model.Trade
import com.example.mvvmkotlincoroutineretrofitdemo.model.Transaction
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.math.BigDecimal
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainRepository {

    private val apiRate = RetrofitManager.apiRate

    val rateSuccessLiveData = MutableLiveData<MutableList<Rate>>()
    val rateFailureLiveData = MutableLiveData<Boolean>()

    /*
    this fun is suspend fun means it will execute in different thread
     */
    suspend fun getRatesForTime() {

        try {

            //here api calling became so simple just 1 line of code
            //there is no callback needed

            val response = apiRate.getRatesForTime().await()

            Log.d(TAG, "$response")

            if (response.isSuccessful) {
                Log.d(TAG, "SUCCESS")
                Log.d(TAG, "${response.body()}")
                rateSuccessLiveData.postValue(response.body())

            } else {
                Log.d(TAG, "FAILURE")
                Log.d(TAG, "${response.body()}")
                rateFailureLiveData.postValue(true)
            }

        } catch (e: UnknownHostException) {
            Log.e(TAG, e.message)
            //this exception occurs when there is no internet connection or host is not available
            //so inform user that something went wrong
            rateFailureLiveData.postValue(true)
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, e.message)
            //this exception occurs when time out will happen
            //so inform user that something went wrong
            rateFailureLiveData.postValue(true)
        } catch (e: Exception) {
            Log.e(TAG, e.message)
            //this is generic exception handling
            //so inform user that something went wrong
            rateFailureLiveData.postValue(true)
        }

    }

    private val apiTransTrades = RetrofitManager.apiTransTrades

    var transSuccessLiveData = MutableLiveData<MutableList<Transaction>>()
    var transFailureLiveData = MutableLiveData<Boolean>()
    var balancesCalculated = MutableLiveData<Boolean>()
    suspend fun getTrans() {

        try {

            //here api calling became so simple just 1 line of code
            //there is no callback needed

            val response = apiTransTrades.getTrans().await()

            Log.d(TAG, "$response")

            if (response.isSuccessful) {
                Log.d(TAG, "SUCCESS")
                Log.d(TAG, "${response.body()}")
                transSuccessLiveData.postValue(response.body())
                if (!transSuccessLiveData.value.isNullOrEmpty())
                    transSuccessLiveData.value!!.sortBy { it.dateTime }

            } else {
                Log.d(TAG, "FAILURE")
                Log.d(TAG, "${response.body()}")
                transFailureLiveData.postValue(true)
            }

        } catch (e: UnknownHostException) {
            Log.e(TAG, e.message)
            //this exception occurs when there is no internet connection or host is not available
            //so inform user that something went wrong
            transFailureLiveData.postValue(true)
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, e.message)
            //this exception occurs when time out will happen
            //so inform user that something went wrong
            transFailureLiveData.postValue(true)
        } catch (e: Exception) {
            Log.e(TAG, e.message)
            //this is generic exception handling
            //so inform user that something went wrong
            transFailureLiveData.postValue(true)
        }

    }

    var tradesSuccessLiveData = MutableLiveData<MutableList<Trade>>()
    var tradesFailureLiveData = MutableLiveData<Boolean>()


    suspend fun getTrades() {

        try {

            //here api calling became so simple just 1 line of code
            //there is no callback needed

            val response = apiTransTrades.getTrades().await()

            Log.d(TAG, "$response")

            if (response.isSuccessful) {
                Log.d(TAG, "SUCCESS")
                Log.d(TAG, "${response.body()}")
                tradesSuccessLiveData.postValue(response.body())
                if (!tradesSuccessLiveData.value.isNullOrEmpty())
                    tradesSuccessLiveData.value!!.sortBy { it.dateTime }


            } else {
                Log.d(TAG, "FAILURE")
                Log.d(TAG, "${response.body()}")
                tradesFailureLiveData.postValue(true)
            }

        } catch (e: UnknownHostException) {
            Log.e(TAG, e.message)
            //this exception occurs when there is no internet connection or host is not available
            //so inform user that something went wrong
            tradesFailureLiveData.postValue(true)
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, e.message)
            //this exception occurs when time out will happen
            //so inform user that something went wrong
            tradesFailureLiveData.postValue(true)
        } catch (e: Exception) {
            Log.e(TAG, e.message)
            //this is generic exception handling
            //so inform user that something went wrong
            tradesFailureLiveData.postValue(true)
        }

    }

    var balancesAtTheEnd = MutableLiveData<MutableMap<String, BigDecimal?>>()

    suspend fun countingBalance(
        trades: MutableList<Trade>,
        transactions: MutableList<Transaction>
    ) {
        if (!trades.isNullOrEmpty()) {
            trades.sortBy { it.dateTime }
        }
        if (!transactions.isNullOrEmpty()) {
            transactions.sortBy { it.dateTime }
        }
        if ((!transactions.isNullOrEmpty()) and (!trades.isNullOrEmpty())) {
            balancesAtTheEnd.postValue(balanceForDate(dateTimeFormatter("2019-11-27T15:29:05")))
        }




    }


    suspend private fun balanceForDate(date: LocalDateTime): MutableMap<String, BigDecimal?>{
                val result: MutableMap<String, BigDecimal?> = mutableMapOf()
                var flagTrades = false
                var flagTransactions = false
                var nextTrade: Trade? = null
                var nextTransaction: Transaction? = null
                if (tradesSuccessLiveData.value == null) {
                    flagTrades = true
                } else {
                    nextTrade = tradesSuccessLiveData.value!![0]
                }
                if (transSuccessLiveData.value == null) {
                    flagTransactions = true
                } else {
                    nextTransaction = transSuccessLiveData.value!![0]
                }

                var i = 1
                while ((!flagTrades) or (!flagTransactions)) {

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
                                    result[nextTrade.tradedPriceCurrency] =
                                        result[nextTrade.tradedPriceCurrency]?.plus(nextTrade.tradedPrice * nextTrade.tradedQuantity - nextTrade.commission)
                                }
                                ("Buy") -> {
                                    result[nextTrade.tradedQuantityCurrency] =
                                        result[nextTrade.tradedQuantityCurrency]?.plus(nextTrade.tradedQuantity - nextTrade.commission)
                                    result[nextTrade.tradedPriceCurrency] =
                                        result[nextTrade.tradedPriceCurrency]?.minus(nextTrade.tradedPrice * nextTrade.tradedQuantity)
                                }
                            }
                        }
                    }


                    if (!flagTransactions) {
                        if (dateTimeFormatter(nextTransaction!!.dateTime) > date) {
                            flagTransactions = true
                        } else {

                            if (!result.keys.contains(nextTransaction.currency))
                                result[nextTransaction.currency] = BigDecimal(0.0)
                            if (nextTransaction.transactionStatus == "Complete") {
                                if (nextTransaction.transactionType == "Deposit")
                                    result[nextTransaction.currency] =
                                        result[nextTransaction.currency]?.plus(nextTransaction.amount - nextTransaction.commission)
                                else {
                                    result[nextTransaction.currency] =
                                        result[nextTransaction.currency]?.minus(nextTransaction.amount + nextTransaction.commission)
                                }
                            }
                        }
                    }

                    if (!flagTrades) {
                        if (tradesSuccessLiveData.value!!.size == i) {
                            flagTrades = true
                        } else {
                            nextTrade = tradesSuccessLiveData.value!![i]
                        }
                    }
                    if (!flagTransactions) {
                        if (transSuccessLiveData.value!!.size == i) {
                            flagTransactions = true
                        } else {
                            nextTransaction = transSuccessLiveData.value!![i]
                        }
                    }
                    i++
                }

                return result
            }

        companion object {
            val TAG = MainRepository::class.java.simpleName
        }
        private fun dateTimeFormatter(string: String): LocalDateTime {
            return LocalDateTime.parse(string, DateTimeFormatter.ISO_DATE_TIME)
        }
    }