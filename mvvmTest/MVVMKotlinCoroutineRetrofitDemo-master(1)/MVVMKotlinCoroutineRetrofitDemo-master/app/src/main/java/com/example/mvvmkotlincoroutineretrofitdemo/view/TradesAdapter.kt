package com.example.mvvmkotlincoroutineretrofitdemo.view


import androidx.lifecycle.MutableLiveData
import com.example.mvvmkotlincoroutineretrofitdemo.model.Trade

class TradesAdapter {

    private var tradeList: MutableList<Trade> = ArrayList()
    var tradesDownloaded = MutableLiveData<Boolean>()

    fun setTrades(rates: MutableList<Trade>) {

        tradeList.addAll(rates)
        tradesDownloaded.postValue(true)


    }




}