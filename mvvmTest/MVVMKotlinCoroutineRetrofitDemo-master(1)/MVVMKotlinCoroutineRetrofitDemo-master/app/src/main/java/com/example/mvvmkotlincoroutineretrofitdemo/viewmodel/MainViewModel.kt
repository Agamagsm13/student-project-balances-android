package com.example.mvvmkotlincoroutineretrofitdemo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mvvmkotlincoroutineretrofitdemo.repository.MainRepository
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val mainRepository = MainRepository()

    val rateSuccessLiveData = mainRepository.rateSuccessLiveData
    val rateFailureLiveData = mainRepository.rateFailureLiveData

    val transSuccessLiveData = mainRepository.transSuccessLiveData
    val transFailureLiveData = mainRepository.transFailureLiveData

    val tradesSuccessLiveData = mainRepository.tradesSuccessLiveData
    val tradesFailureLiveData = mainRepository.tradesFailureLiveData

    fun getRates() {

        viewModelScope.launch { mainRepository.getRates() }

    }
    fun getTrans() {

        viewModelScope.launch { mainRepository.getTrans() }

    }
    fun getTrades() {

        viewModelScope.launch { mainRepository.getTrades() }

    }
}