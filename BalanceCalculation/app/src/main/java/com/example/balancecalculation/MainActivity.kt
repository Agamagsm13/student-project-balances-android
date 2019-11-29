package com.example.balancecalculation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import java.sql.Time
import java.sql.Timestamp
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    var idViewed : ArrayList<Int> = ArrayList<Int>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var text:TextView = findViewById(R.id.test)
        var transactions: ArrayList<Transaction> = ArrayList<Transaction>()
        transactions.add(Transaction("2019-10-01 10:10:10", Type.Deposit, Curr.BTC, 100.0, 0.0f, Status.Complete, 1))
        transactions.add(Transaction("2019-10-01 10:10:11", Type.Withdraw, Curr.BTC, 200.0, 0.0f, Status.Complete, 2))
        transactions.add(Transaction("2019-10-01 10:10:12", Type.Deposit, Curr.BTC, 450.0, 0.0f, Status.Complete, 3))
        transactions.add(Transaction("2019-10-01 10:10:13", Type.Deposit, Curr.RUB, 100.0, 0.0f, Status.Complete, 4))
        transactions.add(Transaction("2019-10-01 10:10:14", Type.Deposit, Curr.RUB, 200.0, 0.0f, Status.Complete, 5))
        transactions.add(Transaction("2019-10-01 10:10:15", Type.Withdraw, Curr.RUB, 450.0, 0.0f, Status.Complete, 6))

        var balances:MutableMap<Curr, Double> = mutableMapOf()
        var exception : Int = -1
        var negativeBalances:MutableMap<Curr, Double> = mutableMapOf()
        var groupedByCurrency = groupByCurrency(transactions)
        for (key in groupedByCurrency.keys){
            balances.set(key, balance(key, groupedByCurrency.getValue(key)))
        }
        for (balance in balances){
            if (balance.value < 0){
                exception = 1
                negativeBalances.set(balance.key, balance.value)
            }
        }
        if (!negativeBalances.isEmpty()){

        }

        text.text = balances.toString()
    }

    fun balance(currency: Curr, transactions: ArrayList<Transaction>):Double{
        var balance = 0.0
        var minBalance = 0.0
        for (transaction in transactions){
            if (transaction.status == Status.Complete){
                if(!idViewed.contains(transaction.id)){
                    idViewed.add(transaction.id)
                    if (transaction.type == Type.Deposit)
                        balance += transaction.amount
                    else {
                        balance -= transaction.amount
                        if (balance < minBalance){
                            minBalance = balance
                        }
                    }
                }
            }
        }
        if (minBalance < 0)
            balance = minBalance
        return balance
    }

    fun groupByCurrency(transactions: ArrayList<Transaction>):MutableMap<Curr, ArrayList<Transaction>>{
        var groupedTransactions :MutableMap<Curr,ArrayList<Transaction>> = mutableMapOf()
        for (transaction in transactions){
            if (!groupedTransactions.containsKey(transaction.currency))
                groupedTransactions.put(transaction.currency, ArrayList<Transaction>())
            groupedTransactions.getValue(transaction.currency).add(transaction)
        }
        return groupedTransactions
    }
}
