package com.example.mvvmkotlincoroutineretrofitdemo.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mvvmkotlincoroutineretrofitdemo.R
import com.example.mvvmkotlincoroutineretrofitdemo.model.Trade

class TradesAdapter : RecyclerView.Adapter<TradesAdapter.TradesViewHolder>() {

    private var tradeList: MutableList<Trade> = ArrayList()

    fun setTrades(rates: MutableList<Trade>) {

        tradeList.addAll(rates)
        notifyDataSetChanged()

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TradesViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)

        return TradesViewHolder(view)
    }

    override fun getItemCount(): Int {

        return tradeList.size
    }

    override fun onBindViewHolder(holder: TradesViewHolder, position: Int) {

        val trade = tradeList[position]

        holder.id.text = trade.id.toString()
        holder.date.text = trade.commission.toString()

    }

    class TradesViewHolder(parent: View) : RecyclerView.ViewHolder(parent) {

        val id = parent.findViewById<TextView>(R.id.userId)
        val date = parent.findViewById<TextView>(R.id.name)

    }

}