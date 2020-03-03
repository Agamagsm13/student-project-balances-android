package com.example.mvvmkotlincoroutineretrofitdemo.view


import androidx.lifecycle.MutableLiveData
import com.example.mvvmkotlincoroutineretrofitdemo.model.Transaction

class TransAdapter  {

    private var transactionList: MutableList<Transaction> = ArrayList()
    var transDownloaded = MutableLiveData<Boolean>()

    fun setTrans(trans: MutableList<Transaction>) {

        transactionList.addAll(trans)
        transDownloaded.postValue(true)

    }




}