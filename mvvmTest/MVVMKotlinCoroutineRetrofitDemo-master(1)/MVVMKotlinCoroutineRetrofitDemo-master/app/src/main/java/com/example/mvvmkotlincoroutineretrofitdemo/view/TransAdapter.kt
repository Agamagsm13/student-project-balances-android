package com.example.mvvmkotlincoroutineretrofitdemo.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mvvmkotlincoroutineretrofitdemo.R
import com.example.mvvmkotlincoroutineretrofitdemo.model.Transaction

class TransAdapter : RecyclerView.Adapter<TransAdapter.TransViewHolder>() {

    private var transactionList: MutableList<Transaction> = ArrayList()

    fun setTrans(rates: MutableList<Transaction>) {

        transactionList.addAll(rates)
        notifyDataSetChanged()

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)

        return TransViewHolder(view)
    }

    override fun getItemCount(): Int {

        return transactionList.size
    }

    override fun onBindViewHolder(holder: TransViewHolder, position: Int) {

        val transaction = transactionList[position]

        holder.id.text = transaction.id.toString()
        holder.date.text = transaction.dateTime

    }

    class TransViewHolder(parent: View) : RecyclerView.ViewHolder(parent) {

        val id = parent.findViewById<TextView>(R.id.userId)
        val date = parent.findViewById<TextView>(R.id.name)

    }

}