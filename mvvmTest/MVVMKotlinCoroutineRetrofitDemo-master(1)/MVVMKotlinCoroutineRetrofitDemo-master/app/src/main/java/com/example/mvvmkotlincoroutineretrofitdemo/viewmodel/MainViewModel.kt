package com.example.mvvmkotlincoroutineretrofitdemo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mvvmkotlincoroutineretrofitdemo.repository.MainRepository
import kotlinx.coroutines.launch
import java.math.BigDecimal

class MainViewModel : ViewModel() {

    private val mainRepository = MainRepository()

    val rateSuccessLiveData = mainRepository.rateSuccessLiveData
    val rateFailureLiveData = mainRepository.rateFailureLiveData

    val transSuccessLiveData = mainRepository.transSuccessLiveData
    val transFailureLiveData = mainRepository.transFailureLiveData

    val tradesSuccessLiveData = mainRepository.tradesSuccessLiveData
    val tradesFailureLiveData = mainRepository.tradesFailureLiveData
    var balancesAtTheEnd = mainRepository.balancesAtTheEnd



    fun getRates(instrument: String, timeFrom : Long, timeTo : Long) {

        viewModelScope.launch { mainRepository.getRatesForTime(instrument, timeFrom, timeTo) }

    }
    fun getTrans() {

        viewModelScope.launch { mainRepository.getTrans() }

    }
    fun getTrades() {

        viewModelScope.launch { mainRepository.getTrades() }

    }
    fun countingBalance() {
        viewModelScope.launch { mainRepository.countingBalance(tradesSuccessLiveData.value!!, transSuccessLiveData.value!!) }
    }




}