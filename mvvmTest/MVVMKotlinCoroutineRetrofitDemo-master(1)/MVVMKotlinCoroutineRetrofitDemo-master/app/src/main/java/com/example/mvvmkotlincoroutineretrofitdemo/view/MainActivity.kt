package com.example.mvvmkotlincoroutineretrofitdemo.view

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mvvmkotlincoroutineretrofitdemo.R
import com.example.mvvmkotlincoroutineretrofitdemo.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var rateAdapter: RateAdapter
    private lateinit var transAdapter: TransAdapter
    private lateinit var tradesAdapter: TradesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        rateAdapter = RateAdapter()
        transAdapter = TransAdapter()
        tradesAdapter = TradesAdapter()

        //setting layout manager to recycler view and adapter
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = tradesAdapter

        //before calling api register live data observer
        registerObservers()

        //calling user list api
        mainViewModel.getTrades()

    }

    private fun registerObservers() {

        mainViewModel.tradesSuccessLiveData.observe(this, Observer { rateList ->

            //if it is not null then we will display all users
            rateList?.let {
                tradesAdapter.setTrades(it)
            }
        })

        mainViewModel.tradesFailureLiveData.observe(this, Observer { isFailed ->

            //if it is not null then we will display all users
            isFailed?.let {
                Toast.makeText(this, "Oops! something went wrong", Toast.LENGTH_SHORT).show()
            }
        })

    }
}
