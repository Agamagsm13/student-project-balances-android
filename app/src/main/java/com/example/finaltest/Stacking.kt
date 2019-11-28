package com.example.finaltest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import com.aachartmodel.aainfographics.aainfographicsLib.aachartConfiger.AAChartView
import com.example.chartcorekotlin.AAChartConfiger.*

class Stacking : AppCompatActivity() {

    private var aaChartView: AAChartView? = null
    private var aaChartModel: AAChartModel? = null
    private var radio1: RadioButton? = null
    private var radio2: RadioButton? = null
    private var radio3: RadioButton? = null
    private var intentMain: Intent? = null
    private var intentCluster: Intent? = null
    private var intentCorrelation: Intent? = null
    private var intentBar: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stacking)
        aaChartView = findViewById(R.id.AAChartView)
        val button1: Button = findViewById(R.id.Button1)
        val button2: Button = findViewById(R.id.Button2)
        val button3: Button = findViewById(R.id.Button3)
        val button4: Button = findViewById(R.id.Button4)
        val button5: Button = findViewById(R.id.Button5)
        radio1 = findViewById(R.id.stacking1)
        radio2 = findViewById(R.id.stacking2)
        radio3 = findViewById(R.id.stacking3)
        intentMain = Intent(this, MainActivity::class.java)
        intentCluster = Intent(this, Cluster::class.java)
        intentCorrelation = Intent(this, Correlation::class.java)
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
        button3Click()

    }
    fun button1Click(){
        startActivity(intentMain)
    }
    fun button2Click(){
        startActivity(intentCluster)
    }
    fun button3Click(){
        radio3?.isChecked = true
        aaChartModel = AAChartModel()
            .title("Portfolio cost")
            .titleFontColor("#afeeee")
            .titleFontSize(20f)
            .titleFontWeight(AAChartFontWeightType.Bold)
            .subtitle("")
            .yAxisTitle("Percent: %")
            .chartType(AAChartType.Column)
            .axesTextColor("#afeeee")
            .dataLabelsFontColor("#afeeee")
            .dataLabelsFontWeight(AAChartFontWeightType.Regular)
            .legendEnabled(false)
            .yAxisMax(100f)
            .stacking(AAChartStackingType.Percent)
            .colorsTheme(arrayOf("#6BEBFF","#61A8E8","#7761E8"))
            .animationType(AAChartAnimationType.Bounce)
            .animationDuration(2000)
            .backgroundColor("#240b3b")
            .categories(arrayOf("2015", "2016", "2017", "2018", "2019"))
            .dataLabelsEnabled(true)
            .series(arrayOf(
                AASeriesElement()
                    .name("Apple Inc.")
                    .data(arrayOf(20, 25, 22, 34, 27))
                    .stack("1"),
                AASeriesElement()
                    .name("Google")
                    .data(arrayOf(30, 20, 25, 14, 23))
                    .stack("1"),
                AASeriesElement()
                    .name("DSX")
                    .data(arrayOf(50, 55, 53, 52, 50))
                    .stack("1")
            ))

        aaChartView?.aa_drawChartWithChartModel(aaChartModel!!)
    }
    fun button4Click(){
        startActivity(intentCorrelation)
    }
    fun button5Click(){
        startActivity(intentBar)
    }
}
