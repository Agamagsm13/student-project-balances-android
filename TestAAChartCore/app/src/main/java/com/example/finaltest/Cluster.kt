package com.example.finaltest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import com.aachartmodel.aainfographics.aainfographicsLib.aachartConfiger.AAChartView
import com.example.chartcorekotlin.AAChartConfiger.*

class Cluster : AppCompatActivity() {


    private var aaChartView: AAChartView? = null
    private var aaChartModel: AAChartModel? = null
    private var radio1: RadioButton? = null
    private var radio2: RadioButton? = null
    private var radio3: RadioButton? = null
    private var intentMain: Intent? = null
    private var intentStack: Intent? = null
    private var intentCorrelation: Intent? = null
    private var intentBar: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cluster)
        aaChartView = findViewById(R.id.AAChartView)
        val button1: Button = findViewById(R.id.Button1)
        val button2: Button = findViewById(R.id.Button2)
        val button3: Button = findViewById(R.id.Button3)
        val button4: Button = findViewById(R.id.Button4)
        val button5: Button = findViewById(R.id.Button5)
        intentMain = Intent(this, MainActivity::class.java)
        intentStack = Intent(this, Stacking::class.java)
        intentCorrelation = Intent(this, Correlation::class.java)
        intentBar = Intent(this, Bar::class.java)
        radio1 = findViewById(R.id.stacking1)
        radio2 = findViewById(R.id.stacking2)
        radio3 = findViewById(R.id.stacking3)

        button1.setOnClickListener {
            button1Click()
        }
        button2.setOnClickListener {
            button2Click()
        }
        button3.setOnClickListener {
            button3Click()
        }
        button4.setOnClickListener {
            button4Click()
        }
        button5.setOnClickListener {
            button5Click()
        }
        button2Click()
    }
    fun button1Click(){
        startActivity(intentMain)
    }
    fun button2Click(){
        radio2?.isChecked = true
        aaChartModel = AAChartModel()
            .chartType(AAChartType.Areaspline)
            .title("Rate")
            .titleFontColor("#afeeee")
            .titleFontSize(20f)
            .titleFontWeight(AAChartFontWeightType.Bold)
            .subtitle("USD/RUB")
            .subtitleFontColor("#afeeee")
            .subtitleFontSize(15f)
            .subtitleFontWeight(AAChartFontWeightType.Bold)
            .marginright(10f)
            .pointHollow(true)
            .borderRadius(4f)
            .axesTextColor("#afeeee")
            .dataLabelsFontColor("#afeeee")
            .dataLabelsFontSize(1f)
            .backgroundColor("#240b3b")
            .yAxisMin(64.5f)
            .yAxisMax(67f)
            .xAxisTickInterval(2)
            .yAxisTitle("RUB")
            .xAxisReversed(false)
            .yAxisGridLineWidth(0.8f)
            .xAxisGridLineWidth(0.8f)
            .gradientColorEnable(true)
            .markerRadius(4f)
            .markerSymbolStyle(AAChartSymbolStyleType.InnerBlank)
            .xAxisVisible(true)
            .categories(arrayOf("00:00","00:30","01:00","01:30","02:00","02:30","03:00","03:30","04:00","04:30","05:00","05:30","06:00","06:30","07:00","07:30","08:00","08:30","09:00"))
            .legendEnabled(true)
            .colorsTheme(arrayOf(AAGradientColor.berrySmoothieColor()))
            .animationType(AAChartAnimationType.EaseInQuart)
            .animationDuration(1200)
            .series(arrayOf(
                AASeriesElement()
                    .name("USD/RUB")
                    .data(arrayOf(66.35,66.22,66.39,66.61,66.2,66.25,66.4,66.48,66.6,66.4,66.2,66.28,66.1,66.09,66.0,66.1,65.95,66.0,66.21))
            ))

        aaChartView?.aa_drawChartWithChartModel(aaChartModel!!)
    }
    fun button3Click(){
        startActivity(intentStack)
    }
    fun button4Click(){
        startActivity(intentCorrelation)
    }
    fun button5Click(){
        startActivity(intentBar)
    }
}
