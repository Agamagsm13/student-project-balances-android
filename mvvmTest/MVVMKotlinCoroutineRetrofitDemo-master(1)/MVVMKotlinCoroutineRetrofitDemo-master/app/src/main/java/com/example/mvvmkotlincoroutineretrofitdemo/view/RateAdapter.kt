package com.example.mvvmkotlincoroutineretrofitdemo.view


import com.example.mvvmkotlincoroutineretrofitdemo.model.Rate

class RateAdapter {

    private var rateList: MutableList<Rate> = ArrayList()

    fun setRates(rates: MutableList<Rate>) {

        rateList.addAll(rates)

    }





}