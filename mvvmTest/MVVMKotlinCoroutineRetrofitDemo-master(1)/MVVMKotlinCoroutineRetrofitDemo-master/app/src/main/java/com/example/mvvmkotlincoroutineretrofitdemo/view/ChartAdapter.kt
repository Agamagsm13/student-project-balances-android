package com.example.mvvmkotlincoroutineretrofitdemo.view

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData

import com.aachartmodel.aainfographics.AAInfographicsLib.AAChartConfiger.*
import com.aachartmodel.aainfographics.AAInfographicsLib.AAOptionsModel.AAChart
import com.example.mvvmkotlincoroutineretrofitdemo.R
import com.example.mvvmkotlincoroutineretrofitdemo.model.Trade
import java.math.BigDecimal

class ChartAdapter  {

    var categories: MutableList<String> = ArrayList()
    var data : ArrayList<BigDecimal?> = arrayListOf()
    var resultF: ArrayList<Any> = arrayListOf()

    fun setData(dataLiveData: MutableMap<String, BigDecimal?>, aaChartView: AAChartView?) {


        data.addAll(dataLiveData.values)
        categories.addAll(dataLiveData.keys)


        for (i in 0 until data.size){
            resultF.add(arrayOf(categories[i], data[i]!!))
        }

        var aaChartModel = AAChartModel()
            .title("Portfolio")
            .titleFontColor("#0B1929")
            .titleFontSize(20f)
            .subtitleFontColor("#0B1929")
            .titleFontWeight(AAChartFontWeightType.Bold)
            .subtitle("2019")
            .yAxisTitle("Values in $")
            .legendEnabled(true)
            .chartType(AAChartType.Pie)
            .axesTextColor("#0B1929")
            .dataLabelsFontColor("#0B1929")
            .dataLabelsFontWeight(AAChartFontWeightType.Regular)
            .legendEnabled(false)
            .stacking(AAChartStackingType.Normal)
            .colorsTheme(
                arrayOf(
                    "#306FB3",
                    "#7291B3",
                    "#80A5CC",
                    "#A1CEFF",
                    "#8184CC",
                    "#A1A5FF",
                    "#A56AFF",
                    "#C6A1FF",
                    "#9F81CC",
                    "#DCA1FF",
                    "#322280",
                    "#4732B3",
                    "#8AADFE",
                    "#2B4C99",
                    "#2A9695"
                )
            )
            .animationType(AAChartAnimationType.Bounce)
            .categories(categories.toTypedArray())
            .animationDuration(2000)
            .series(
                arrayOf(
                    AASeriesElement()
                        .name("in $")
                        .data(
                            resultF.toArray()
                        )
                )
            )


        aaChartView!!.aa_drawChartWithChartModel(aaChartModel)


    }






}