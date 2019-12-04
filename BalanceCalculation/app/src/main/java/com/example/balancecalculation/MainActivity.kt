package com.example.balancecalculation

import android.content.DialogInterface
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import co.metalab.asyncawait.async
import co.metalab.asyncawait.await
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result


import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

import java.sql.Time
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    var idViewed : ArrayList<Int> = ArrayList<Int>()
    var transactions: ArrayList<Transaction> = ArrayList()
    var balances:MutableMap<String, Pair<Double, Double>> = mutableMapOf()
    var allTrades:ArrayList<Trade>? = arrayListOf()
    var allTransactions:ArrayList<Transaction>? = arrayListOf()

    val urlTrades = "http://3.248.170.197:9999/bcv/trades"
    val urlTransactions = "http://3.248.170.197:9999/bcv/transactions"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var text:TextView = findViewById(R.id.test)
        var addTransactionButton: FloatingActionButton = findViewById(R.id.addTransactions)
        addTransactionButton.setBackgroundColor(Color.BLUE)
        addTransactionButton.setOnClickListener{

        }

        async {
            allTrades = await { urlTrades.httpGet().responseObject<ArrayList<Trade>>().third.get() }
            allTransactions = await {
                urlTransactions.httpGet().responseObject<ArrayList<Transaction>>().third.get()
            }

            var grouped = groupTransactionsByMonth(allTransactions)
            var st: String = ""
            var resultTransactions: MutableMap<Int, MutableMap<String, Double?>> = mutableMapOf()
            for (i in 1..12) {
                resultTransactions[i] = calcTransForMonth(grouped[i])
            }
            var tr= groupTransactionsByYear(allTransactions)
            st = tr.keys.toString() + tr.values
            text.text = st
        }

    }

    fun groupTransactionsByYear(transactions:ArrayList<Transaction>?):MutableMap<Int,  ArrayList<Transaction>>{
        var yearTransactions:MutableMap<Int, ArrayList<Transaction>> = mutableMapOf()
        var year = 0
        for (transaction in transactions!!){
            year  = dateTimeFormmatter(transaction.dateTime).year
            yearTransactions[year] = ArrayList()
        }
        if (transactions != null){
            for (transaction in transactions){
                yearTransactions.getValue(year).add(transaction)

            }
        }
        return yearTransactions
    }

    fun groupTransactionsByMonth(transactions:ArrayList<Transaction>?):MutableMap<Int,  ArrayList<Transaction>>{
        var monthlyTransactions:MutableMap<Int, ArrayList<Transaction>> = mutableMapOf()
        var text:TextView = findViewById(R.id.test)
        for (i in 1..12) {
            monthlyTransactions[i] = ArrayList()
        }
        var month = 0
        if (transactions != null){
            for (transaction in transactions){
                month = dateTimeFormmatter(transaction.dateTime).monthValue
                text.text = month.toString()

                monthlyTransactions.getValue(month).add(transaction)

            }
        }
        return monthlyTransactions
    }


    fun calcTransForMonth(transactions: ArrayList<Transaction>?):MutableMap<String,Double?>{
        var result:MutableMap<String, Double?> = mutableMapOf()
        if (transactions != null){
            for (transaction in transactions){
                if (transaction.transactionStatus == "Complete"){
                    if (result.containsKey(transaction.currency)){
                        if (transaction.transactionType == "Deposit")
                            result[transaction.currency] = result[transaction.currency]?.plus(transaction.amount - transaction.commission)
                        else {
                            result[transaction.currency] = result[transaction.currency]?.minus(transaction.amount + transaction.commission)
                        }
                    }
                    else{
                        if (transaction.transactionType == "Deposit")
                            result[transaction.currency] = transaction.amount
                        else
                            result[transaction.currency] = -transaction.amount
                    }
                }
            }
        }
        return result
    }



    fun dateTimeFormmatter(string:String):LocalDateTime{
        val parsedDate = LocalDateTime.parse(string, DateTimeFormatter.ISO_DATE_TIME)
        return parsedDate
    }

    fun getAllTrades(){
        "http://3.248.170.197:9999/bcv/trades"
            .httpGet()
            .responseObject<ArrayList<Trade>> {
                    _, _, result ->
                when (result) {

                    is  Result.Success -> {
                        allTrades = result.get()

                    }

                    is Result.Failure ->
                    {
                        println("--------------------------")
                        println(result.getException())
                        println("--------------------------")
                    }
                }
            }
    }

    fun getAllTransactions(){
        "http://3.248.170.197:9999/bcv/transactions"
            .httpGet()
            .responseObject<ArrayList<Transaction>> {
                    _, _, result ->
                when (result) {

                    is  Result.Success -> {
                        allTransactions = result.get()

                    }

                    is Result.Failure ->
                    {
                        println("--------------------------")
                        println(result.getException())
                        println("--------------------------")
                    }
                }
            }
    }



   // }
}
