package com.example.mvvmkotlincoroutineretrofitdemo.view

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.aachartmodel.aainfographics.AAInfographicsLib.AAChartConfiger.AAChartView
import com.example.mvvmkotlincoroutineretrofitdemo.R
import com.example.mvvmkotlincoroutineretrofitdemo.viewmodel.MainViewModel
import java.math.BigDecimal

class MainActivity : AppCompatActivity() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var rateAdapter: RateAdapter
    private lateinit var transAdapter: TransAdapter
    private lateinit var tradesAdapter: TradesAdapter
    private lateinit var chartAdapter: ChartAdapter

    private var aaChartView: AAChartView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        aaChartView = findViewById(R.id.AAChartView)

        mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        rateAdapter = RateAdapter()
        transAdapter = TransAdapter()
        tradesAdapter = TradesAdapter()
        chartAdapter = ChartAdapter()

        //setting layout manager to recycler view and adapter



        //before calling api register live data observer
        registerObservers()

        //calling user list api
        mainViewModel.getTrades()
        mainViewModel.getTrans()

    }

    private fun registerObservers() {

        mainViewModel.tradesSuccessLiveData.observe(this, Observer { tradesList ->

            //if it is not null then we will display all users
            tradesList?.let {
                tradesAdapter.setTrades(it)
            }
        })
        mainViewModel.transSuccessLiveData.observe(this, Observer { transactionList ->

            //if it is not null then we will display all users
            transactionList?.let {
                transAdapter.setTrans(it)
            }
        })

        mainViewModel.tradesFailureLiveData.observe(this, Observer { isFailed ->

            //if it is not null then we will display all users
            isFailed?.let {
                Toast.makeText(this, "Oops! something went wrong", Toast.LENGTH_SHORT).show()
            }
        })
        mainViewModel.transFailureLiveData.observe(this, Observer { isFailed ->

            //if it is not null then we will display all users
            isFailed?.let {
                Toast.makeText(this, "Oops! something went wrong", Toast.LENGTH_SHORT).show()
            }
        })

        tradesAdapter.tradesDownloaded.observe(this, Observer {


            if (transAdapter.transDownloaded.value == true){
                Toast.makeText(this, "Wow4", Toast.LENGTH_SHORT).show()
                val dat: ArrayList<BigDecimal?> = arrayListOf()


                mainViewModel.countingBalance()

            }
        })
        transAdapter.transDownloaded.observe(this, Observer {
            if (tradesAdapter.tradesDownloaded.value == true){
                Toast.makeText(this, "Wow3", Toast.LENGTH_SHORT).show()

                mainViewModel.countingBalance()

            }
        })
        mainViewModel.balancesAtTheEnd.observe(this, Observer {
                chartAdapter.setData(mainViewModel.balancesAtTheEnd.value!!, aaChartView)

            }
        )

    }
}
