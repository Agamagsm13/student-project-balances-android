package com.example.finaltest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import com.aachartmodel.aainfographics.aainfographicsLib.aachartConfiger.AAChartView
import com.example.chartcorekotlin.AAChartConfiger.*

class Correlation : AppCompatActivity() {

    private var aaChartView: AAChartView? = null
    private var aaChartModel: AAChartModel? = null
    private var intentMain: Intent? = null
    private var intentCluster: Intent? = null
    private var intentStack: Intent? = null
    private var intentBar: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_correlation)
        aaChartView = findViewById(R.id.AAChartView)
        val button1: Button = findViewById(R.id.Button1)
        val button2: Button = findViewById(R.id.Button2)
        val button3: Button = findViewById(R.id.Button3)
        val button4: Button = findViewById(R.id.Button4)
        val button5: Button = findViewById(R.id.Button5)
        intentMain = Intent(this, MainActivity::class.java)
        intentCluster = Intent(this, Cluster::class.java)
        intentStack = Intent(this, Stacking::class.java)
        intentBar = Intent(this, Bar::class.java)

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
        button4Click()

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

        val notNormalizedData1 = arrayListOf<Float>(7212.21f, 7232.31f, 7180.27f, 7230f, 7267.41f, 7200.51f, 7256.21f, 7193.98f, 7200.21f, 7220.24f, 7160.38f, 7217.31f, 7247.25f, 7212.29f, 7229.31f, 7174.21f, 7200.25f, 7370.24f, 7340.34f)
        val notNormalizedData2 = arrayListOf<Float>(462663.60f, 460664.50f, 459765.40f, 454038.49f, 462302.74f, 465824.95f, 460634.50f, 450183.30f, 439454.57f, 441141.98f, 443750.67f, 447029.44f, 445084.01f, 450585.50f, 457930.86f, 454785.47f, 460858.50f, 469363.57f, 465001.36f)

        var normalizedData1 = normalize(notNormalizedData1)
        var normalizedData2 = normalize(notNormalizedData2)


        aaChartModel = AAChartModel()
            .title("Kak nazvat'?")
            .titleFontColor("#afeeee")
            .titleFontSize(20f)
            .titleFontWeight(AAChartFontWeightType.Bold)
            .xAxisVisible(true)
            .yAxisVisible(true)
            .subtitle("")
            .yAxisTitle("Normalized Values")
            .chartType(AAChartType.Line)
            .axesTextColor("#afeeee")
            .legendEnabled(true)
            .markerSymbol(AAChartSymbolType.Circle)
            .colorsTheme(arrayOf("#EF9335","#9C82F0"))
            .yAxisGridLineWidth(0.4f)
            .xAxisGridLineWidth(0.4f)
            .animationType(AAChartAnimationType.EaseInQuart)
            .animationDuration(2000)
            .backgroundColor("#240b3b")
            .categories(arrayOf("00:00","00:30","01:00","01:30","02:00","02:30","03:00","03:30","04:00","04:30","05:00","05:30","06:00","06:30","07:00","07:30","08:00","08:30","09:00"))
            .series(arrayOf(
                AASeriesElement()
                    .name("USD")
                    .data(normalizedData1.toArray()),
                AASeriesElement()
                    .name("RUB")
                    .data(normalizedData2.toArray())
            ))
        aaChartView?.aa_drawChartWithChartModel(aaChartModel!!)
    }

    fun button5Click(){
        startActivity(intentBar)
    }

    fun normalize(arrayToNormalize:ArrayList<Float>):ArrayList<Float>{
        var minData = arrayToNormalize.min()!!
        var maxData = arrayToNormalize.max()!!
        var resultData : ArrayList<Float> = ArrayList()
        for (number in arrayToNormalize){
            resultData.add(((number - minData)*100)/(maxData - minData))
        }
    return resultData
    }
}

