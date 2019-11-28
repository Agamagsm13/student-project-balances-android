package com.example.finaltest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.aachartmodel.aainfographics.aainfographicsLib.aachartConfiger.AAChartView
import com.example.chartcorekotlin.AAChartConfiger.*

class Bar : AppCompatActivity() {

    private var aaChartView: AAChartView? = null
    private var aaChartModel: AAChartModel? = null
    private var intentMain: Intent? = null
    private var intentCluster: Intent? = null
    private var intentStack: Intent? = null
    private var intentCorrelation: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bar)
        aaChartView = findViewById(R.id.AAChartView)
        val button1: Button = findViewById(R.id.Button1)
        val button2: Button = findViewById(R.id.Button2)
        val button3: Button = findViewById(R.id.Button3)
        val button4: Button = findViewById(R.id.Button4)
        val button5: Button = findViewById(R.id.Button5)
        intentMain = Intent(this, MainActivity::class.java)
        intentCluster = Intent(this, Cluster::class.java)
        intentStack = Intent(this, Stacking::class.java)
        intentCorrelation = Intent(this, Correlation::class.java)

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
        button5Click()
    }

    fun button1Click(){
        startActivity(intentMain)
    }
    fun button2Click(){
        startActivity(intentCluster)
    }
    fun button3Click(){
        startActivity(intentStack)
    }
    fun button4Click(){
        startActivity(intentCorrelation)
    }
    fun button5Click(){
        var deposits = arrayListOf<Float>(100f, 200f, 1000f)
        var outputs = arrayListOf<Float>(500f, 200f)
        var startBalance = 3000f
        var finalBalance = 5000f
        aaChartModel = AAChartModel()
            .title("Kak nazvat'?")
            .titleFontColor("#afeeee")
            .titleFontSize(20f)
            .titleFontWeight(AAChartFontWeightType.Bold)
            .subtitle("")
            .yAxisTitle("Normalized Values")
            .chartType(AAChartType.Bar)
            .axesTextColor("#afeeee")
            .legendEnabled(true)
            .markerSymbol(AAChartSymbolType.Circle)
            .colorsTheme(arrayOf("#EF9335","#9C82F0"))
            .animationType(AAChartAnimationType.EaseInQuart)
            .animationDuration(2000)
            .backgroundColor("#240b3b")
            .series(arrayOf(
                AASeriesElement()
                    .name("Earnings")
                    .data(Profit(deposits, outputs, startBalance, finalBalance).toArray())

            ))
        aaChartView?.aa_drawChartWithChartModel(aaChartModel!!)
    }
    fun Profit(deposits:ArrayList<Float>, outputs:ArrayList<Float>, startBalance:Float, finalBalance:Float):ArrayList<Float>{
        var summaryDeposit:Float = 0f
        var summaryOutput:Float = 0f
        for (item in deposits)
            summaryDeposit += item
        for (item in outputs)
            summaryOutput += item
        var profit:Float = finalBalance - startBalance + summaryOutput
        var result:ArrayList<Float> = ArrayList()
        result.add(profit)
        return result
    }
}
