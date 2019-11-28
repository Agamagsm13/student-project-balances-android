package com.example.finaltest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import androidx.core.view.GestureDetectorCompat
import com.aachartmodel.aainfographics.aainfographicsLib.aachartConfiger.AAChartView
import com.example.chartcorekotlin.AAChartConfiger.*

class MainActivity : AppCompatActivity(){

    private var aaChartView: AAChartView? = null
    private var aaChartModel: AAChartModel? = null
    private var intentCluster: Intent? = null
    private var intentStack: Intent? = null
    private var intentCorrelation: Intent? = null
    private var intentBar: Intent? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        aaChartView = findViewById(R.id.AAChartView)
        intentCluster = Intent(this, Cluster::class.java)
        intentStack = Intent(this, Stacking::class.java)
        intentCorrelation = Intent(this, Correlation::class.java)
        intentBar = Intent(this, Bar::class.java)
        val button1: Button = findViewById(R.id.Button1)
        val button2: Button = findViewById(R.id.Button2)
        val button3: Button = findViewById(R.id.Button3)
        val button4: Button = findViewById(R.id.Button4)
        val button5: Button = findViewById(R.id.Button5)
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
        button1Click()
    }
    fun button1Click(){
        aaChartModel = AAChartModel()
            .chartType(AAChartType.Bar)
            .title("Input/Output")
            .titleFontColor("#afeeee")
            .titleFontSize(20f)
            .titleFontWeight(AAChartFontWeightType.Bold)
            //  .subtitle("subtitle")
            .subtitleFontColor("#afeeee")
            .subtitleFontSize(15f)
            .axesTextColor("#afeeee")
            .dataLabelsFontColor("#afeeee")
            .backgroundColor("#240b3b")
            .borderRadius(0f)
            .yAxisTitle("Dollars")
            .subtitleFontWeight(AAChartFontWeightType.Bold)
            .categories(arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"))
            .legendEnabled(true)
            .colorsTheme(arrayOf(AAGradientColor.berrySmoothieColor(), AAGradientColor.oceanBlueColor()))
            .animationType(AAChartAnimationType.EaseInQuart)
            .xAxisReversed(true)
            .animationDuration(1200)
            .series(arrayOf(
                AASeriesElement()
                    .name("Input $")
                    .data(arrayOf(1000, 1100, 1200, 1300, 800, 0, 1200, 1000, 600, 300, 700, 1000)),
                AASeriesElement()
                    .name("Output $")
                    .data(arrayOf(500, 1200, 400, 700, 400, 200, 800, 600, 50, 100, 1000, 600))
            ))
        aaChartView?.aa_drawChartWithChartModel(aaChartModel!!)
    }
    fun button2Click(){
        startActivity(intentCluster)
    }
    fun button3Click(){
        startActivity(intentStack)
    }
    fun button4Click(){
        startActivity((intentCorrelation))
    }
    fun button5Click(){
        startActivity(intentBar)
    }

}
