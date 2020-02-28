package com.example.mvvmkotlincoroutineretrofitdemo.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mvvmkotlincoroutineretrofitdemo.R
import com.example.mvvmkotlincoroutineretrofitdemo.model.Rate

class RateAdapter : RecyclerView.Adapter<RateAdapter.RateViewHolder>() {

    private var rateList: MutableList<Rate> = ArrayList()

    fun setRates(rates: MutableList<Rate>) {

        rateList.addAll(rates)
        notifyDataSetChanged()

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RateViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)

        return RateViewHolder(view)
    }

    override fun getItemCount(): Int {

        return rateList.size
    }

    override fun onBindViewHolder(holder: RateViewHolder, position: Int) {

        val rate = rateList[position]

        holder.exchangeRate.text = rate.exchangeRate.toString()
        holder.date.text = rate.date.toString()

    }

    class RateViewHolder(parent: View) : RecyclerView.ViewHolder(parent) {

        val exchangeRate = parent.findViewById<TextView>(R.id.userId)
        val date = parent.findViewById<TextView>(R.id.name)

    }

}