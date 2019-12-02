package com.example.balancecalculation

import android.content.DialogInterface
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.sql.Time
import java.sql.Timestamp
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    var idViewed : ArrayList<Int> = ArrayList<Int>()
    var transactions: ArrayList<Transaction> = ArrayList()
    var balances:MutableMap<Curr, Double> = mutableMapOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var addTransactionButton: FloatingActionButton = findViewById(R.id.addTransactions)
        addTransactionButton.setBackgroundColor(Color.BLUE)
        addTransactionButton.setOnClickListener(){
            addTransactions()
        }

    }

    fun balance(currency: Curr, transactions: ArrayList<Transaction>):Double{
        idViewed.clear()
        var balance = 0.0
        var minBalance = 0.0
        for (transaction in transactions){
            if (transaction.status == Status.Complete){
                if(!idViewed.contains(transaction.id)){
                    idViewed.add(transaction.id)
                    if (transaction.type == Type.Deposit)
                        balance += (transaction.amount - transaction.commission)
                    else {
                        balance -= (transaction.amount + transaction.commission)
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

    fun addTransaction(date:String, type:Type, currency: Curr, amount: Double, comission:Float, status: Status, id:Int){
        transactions.add((Transaction(date, type, currency, amount, comission, status, id)))
        showBalances()
    }
    fun addTransactions(){
        transactions.clear()
        transactions.add(Transaction("2019-10-01 10:10:10", Type.Deposit, Curr.BTC, 100.0, 0.0f, Status.Complete, 1))
        transactions.add(Transaction("2019-10-01 10:10:11", Type.Withdraw, Curr.BTC, 200.0, 0.0f, Status.Complete, 2))
        transactions.add(Transaction("2019-10-01 10:10:12", Type.Deposit, Curr.BTC, 450.0, 0.0f, Status.Complete, 3))
        transactions.add(Transaction("2019-10-01 10:10:13", Type.Deposit, Curr.RUB, 100.0, 0.0f, Status.Complete, 4))
        transactions.add(Transaction("2019-10-01 10:10:14", Type.Deposit, Curr.RUB, 200.0, 0.0f, Status.Complete, 5))
        transactions.add(Transaction("2019-10-01 10:10:15", Type.Withdraw, Curr.RUB, 450.0, 0.0f, Status.Complete, 6))
        Toast.makeText(this, "Transactions added", Toast.LENGTH_SHORT).show();
        showBalances()
    }

    fun showBalances(){
        var text:TextView = findViewById(R.id.test)


       // var balances:MutableMap<Curr, Double> = mutableMapOf()
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
        var negativeBalancesString = ""
        for (bal in negativeBalances){
            negativeBalancesString += "${bal.key.toString()} Balance: ${bal.value.toString()}\n"
        }
        if (exception == 1){
            var bui:AlertDialog.Builder = AlertDialog.Builder(this)
            bui.setMessage("Во время рассчёта баланс следующих валют становился отрицателен:\n$negativeBalancesString\n Задать начальный баланс?")
            bui.setNegativeButton("NoNoNo"){dialog, which ->  dialog.cancel()}
            bui.setPositiveButton("OH YEAH!"){dialog, which ->
                for (negativeBalance in negativeBalances){
                  changeStartBalance(negativeBalance.key)

                }

            }
            val alertDialog: AlertDialog = bui.create()
            alertDialog.show()
        }
        else
            text.text = balances.toString()


    }
    var id = -1
    fun changeStartBalance(currency: Curr){
        var text:TextView = findViewById(R.id.test)
        var bui:AlertDialog.Builder = AlertDialog.Builder(this)
        bui.setMessage("Укажите начальный балaнс $currency")

        var input = EditText(this)
        bui.setView(input)
        bui.setPositiveButton("OK"){dialog, which ->
            if (input.text.toString().toDouble()>=abs(balances.getValue(currency))){
                balances[currency] = balances.getValue(currency) + input.text.toString().toDouble()
                // addTransaction()
                text.text = balances.toString()
            }
            else{
                dialog.dismiss()

            }
        }
        bui.show()
    }
}
