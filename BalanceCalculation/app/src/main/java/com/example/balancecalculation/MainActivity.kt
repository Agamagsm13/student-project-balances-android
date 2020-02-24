package com.example.balancecalculation

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.*
import co.metalab.asyncawait.async
import com.aachartmodel.aainfographics.aainfographicsLib.aachartConfiger.AAChartView
import com.example.chartcorekotlin.AAChartConfiger.*
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpGet
import kotlinx.coroutines.delay
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.github.kittinunf.result.Result
import java.util.*
import kotlin.collections.ArrayList
import java.text.SimpleDateFormat
import java.time.ZoneOffset


class MainActivity : AppCompatActivity() {


    var isMenu1Pressed =
        false                                                                         //is there pressed popup menu on the right corner

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inf: MenuInflater = menuInflater
        inf.inflate(R.menu.popup, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu1 -> {
                isMenu1Pressed = true
                start()
                true
            }
            else -> false
        }

    }

    var counterForGraphs = 0
    private var allTrades: ArrayList<Trade>? = arrayListOf()
    private var allTransactions: ArrayList<Transaction>? = arrayListOf()
    private var allAddedTransactions: ArrayList<Transaction>? =
        arrayListOf()                          //transactions that added artificially for positive balances
    private var idForAddTrans = 0
    var showYearsButton: Button? = null
    private var years: ArrayList<Int> = arrayListOf()
    private var yearForSeriesGraph: Int = 2014
    private var isDownloaded =
        false                                                                   //flag that client already downloaded his trades and transactions
    private var aaChartView: AAChartView? = null
    private var aaChartModel: AAChartModel? = null
    var finalBalances =
        mutableMapOf<String, BigDecimal?>()                                            //balances after all trades and transactions


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        aaChartView = findViewById(R.id.AAChartView)
        showYearsButton = findViewById(R.id.PopupButton)

    }


    @SuppressLint("ClickableViewAccessibility")
    private fun start() {
        aaChartView!!.setOnTouchListener(object : OnSwipeTouchListener() {
            override fun onSwipeLeft() {
                counterForGraphs = (counterForGraphs + 1) % 10
                when (counterForGraphs) {
                    1 -> {
                        modelingSeriesForPie(finalBalances)
                        showYearsButton?.visibility = View.INVISIBLE
                    }
                    2 -> {
                        drawGraphInputOutput(modelingSeriesForInputOutput(allTransactions, 2014))
                        showYearsButton?.visibility = View.VISIBLE
                    }
                    3 -> {
                        modelingSeriesForRate("btc")
                        showYearsButton?.visibility = View.INVISIBLE
                    }
                    4 -> {
                        modelingSeriesForIncome("btc")
                        showYearsButton?.visibility = View.INVISIBLE
                    }
                    5 -> {
                        modelingSeriesForIncomePortfolio(finalBalances)
                        showYearsButton?.visibility = View.INVISIBLE
                    }
                    6 -> {
                        modelingSeriesForRateInPortfolio(
                            dateTimeFormatter("2014-11-08T03:00:00"),
                            "btc"
                        )
                        showYearsButton?.visibility = View.INVISIBLE
                    }


                }

            }

            override fun onSwipeRight() {
                counterForGraphs = (counterForGraphs - 1) % 10
                when (counterForGraphs) {
                    0 -> {
                        start()
                        showYearsButton?.visibility = View.VISIBLE
                    }
                    1 -> {
                        modelingSeriesForPie(finalBalances)
                        showYearsButton?.visibility = View.INVISIBLE
                    }
                    2 -> {
                        drawGraphInputOutput(modelingSeriesForInputOutput(allTransactions, 2014))
                        showYearsButton?.visibility = View.VISIBLE
                    }
                    3 -> {
                        modelingSeriesForRate("btc")
                        showYearsButton?.visibility = View.INVISIBLE
                    }
                    4 -> {
                        modelingSeriesForIncome("btc")
                        showYearsButton?.visibility = View.INVISIBLE
                    }
                    5 -> {
                        modelingSeriesForIncomePortfolio(finalBalances)
                        showYearsButton?.visibility = View.INVISIBLE
                    }
                }
            }
        })

        if (!isDownloaded) {
            fff()                                                          //if we will draw column graph again? we won't download all trades and transactions
        } else {
            var popupMenu: PopupMenu?
            showYearsButton?.setOnClickListener {
                popupMenu = PopupMenu(this@MainActivity, showYearsButton)
                for (year in years) {
                    popupMenu!!.menu.add(year.toString())
                }
                popupMenu!!.menuInflater.inflate(R.menu.popup_year, popupMenu!!.menu)
                popupMenu!!.setOnMenuItemClickListener { item ->
                    if (counterForGraphs == 0) {
                        yearForSeriesGraph = item.title.toString().toInt()
                        drawGraphColumn(modelingSeriesForGraph(yearForSeriesGraph))
                    }
                    if (counterForGraphs == 2) {
                        drawGraphInputOutput(
                            modelingSeriesForInputOutput(
                                allTransactions,
                                item.title.toString().toInt()
                            )
                        )
                    }
                    true
                }
                popupMenu!!.show()

            }

            drawGraphColumn(modelingSeriesForGraph(yearForSeriesGraph))
        }

    }


    private fun fff() {
        val httpAsync = "http://3.248.170.197:9999/bcv/trades"
            .httpGet().responseObject<ArrayList<Trade>> { _, _, result ->
                when (result) {
                    is Result.Failure -> {
                        val ex = result.getException()
                        println(ex)
                    }
                    is Result.Success -> {
                        allTrades = result.component1()
                        val httpAsync = "http://3.248.170.197:9999/bcv/transactions"
                            .httpGet().responseObject<ArrayList<Transaction>> { _, _, result ->
                                when (result) {
                                    is Result.Failure -> {
                                        val ex = result.getException()
                                        println(ex)
                                    }
                                    is Result.Success -> {
                                        allTransactions = result.component1()
                                        showYearsButton?.visibility = View.VISIBLE
                                        if (allTrades != null) {
                                            allTrades!!.sortBy { it.dateTime }
                                            years.add(dateTimeFormatter(allTrades!!.first().dateTime).year)                      //add years from first and last trades
                                            if (!years.contains(dateTimeFormatter(allTrades!!.last().dateTime).year))
                                                years.add(dateTimeFormatter(allTrades!!.last().dateTime).year)
                                        }
                                        if (allTransactions != null) {
                                            allTransactions!!.sortBy { it.dateTime }
                                            if (!years.contains(dateTimeFormatter(allTransactions!!.last().dateTime).year))      //add years from first and last transactions
                                                years.add(dateTimeFormatter(allTransactions!!.last().dateTime).year)
                                            if (!years.contains(dateTimeFormatter(allTransactions!!.first().dateTime).year))
                                                years.add(dateTimeFormatter(allTransactions!!.first().dateTime).year)
                                        }

                                        var lastDate: LocalDateTime

                                        if ((allTransactions != null) and (allTrades != null)) {                                  //searching last date
                                            lastDate =
                                                dateTimeFormatter(allTrades!!.last().dateTime)
                                            if (dateTimeFormatter(allTransactions!!.last().dateTime) > lastDate) {
                                                lastDate =
                                                    dateTimeFormatter(allTransactions!!.last().dateTime)
                                            }
                                        } else
                                            lastDate = dateTimeFormatter("2014-12-31T23:59:59")
                                        finalBalances =
                                            balanceForDateOnlyTrades(lastDate)                                        //calculation of balances after all transactions and trades
                                        for (i in (years.min()!!..years.max()!!)) {                                          //here we get sorted list of all trading years
                                            if (!years.contains(i))
                                                years.add(i)
                                        }
                                        years.sort()
                                        isDownloaded = true
                                        var popupMenu: PopupMenu?
                                        showYearsButton?.setOnClickListener {
                                            popupMenu =
                                                PopupMenu(this@MainActivity, showYearsButton)
                                            for (year in years) {
                                                popupMenu!!.menu.add(year.toString())
                                            }
                                            popupMenu!!.menuInflater.inflate(
                                                R.menu.popup_year,
                                                popupMenu!!.menu
                                            )
                                            popupMenu!!.setOnMenuItemClickListener { item ->
                                                if (counterForGraphs == 0) {
                                                    yearForSeriesGraph =
                                                        item.title.toString().toInt()
                                                    drawGraphColumn(
                                                        modelingSeriesForGraph(
                                                            yearForSeriesGraph
                                                        )
                                                    )
                                                }
                                                if (counterForGraphs == 2) {
                                                    drawGraphInputOutput(
                                                        modelingSeriesForInputOutput(
                                                            allTransactions,
                                                            item.title.toString().toInt()
                                                        )
                                                    )
                                                }
                                                true
                                            }
                                            popupMenu!!.show()

                                        }

                                        drawGraphColumn(modelingSeriesForGraph(yearForSeriesGraph))
                                    }
                                }
                            }

                        httpAsync.join()
                    }
                }
            }

        httpAsync.join()
    }

    private fun drawGraphColumn(data: Array<AASeriesElement>) {

        aaChartModel = AAChartModel()
            .title("Portfolio")
            .titleFontColor("#0B1929")
            .titleFontSize(20f)
            .subtitleFontColor("#0B1929")
            .titleFontWeight(AAChartFontWeightType.Bold)
            .subtitle(yearForSeriesGraph.toString())
            .yAxisTitle("Values in $")
            .chartType(AAChartType.Column)
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
            .animationDuration(2000)
            .categories(
                arrayOf(
                    "Jan",
                    "Feb",
                    "Mar",
                    "Apr",
                    "May",
                    "Jun",
                    "Jul",
                    "Aug",
                    "Sep",
                    "Oct",
                    "Nov",
                    "Dec"
                )
            )
            .series(data)

        aaChartView?.aa_drawChartWithChartModel(aaChartModel!!)


    }


    private fun modelingSeriesForRateInPortfolio(date: LocalDateTime, currency: String) {
        var dates: ArrayList<String> = arrayListOf()
        var rates: ArrayList<BigDecimal> = arrayListOf()
        var balanceAtStart = balanceForDateOneCurr(date, currency)
        val formatter = SimpleDateFormat("dd/MM/yyyy")
        var flagTrades = false
        var flagTrans = false
        var counterTrades = 1
        var counterTrans = 0
        var nextTrade = allTrades?.first()
        var nextTransaction = allTransactions?.first()
        if (nextTrade == null)
            flagTrades = true
        if (nextTransaction == null)
            flagTrans = true
        for (i in 0..9) {
            while ((!flagTrades) and ((dateTimeFormatter(nextTrade!!.dateTime) >= date.plusDays(i.toLong())) and (dateTimeFormatter(
                    nextTrade.dateTime
                ) < date.plusDays((i + 1).toLong())))
            ) {
                if (!flagTrades) {
                    if (nextTrade.tradedQuantityCurrency == currency.toUpperCase()) {
                        when (nextTrade.tradeType) {
                            ("Sell") -> {
                                balanceAtStart =
                                    balanceAtStart.minus(nextTrade.tradedQuantity)
                            }
                            ("Buy") -> {
                                balanceAtStart =
                                    balanceAtStart.plus(nextTrade.tradedQuantity - nextTrade.commission)
                            }
                        }
                    }
                    if (nextTrade.tradedPriceCurrency == currency.toUpperCase()) {
                        when (nextTrade.tradeType) {
                            ("Sell") -> {
                                balanceAtStart =
                                    balanceAtStart.plus(nextTrade.tradedQuantity * nextTrade.tradedPrice - nextTrade.commission)
                            }
                            ("Buy") -> {
                                balanceAtStart =
                                    balanceAtStart.minus(nextTrade.tradedQuantity * nextTrade.tradedPrice)
                            }
                        }
                    }
                }
                if (!flagTrades) {
                    if (allTrades!!.size == counterTrades) {
                        flagTrades = true
                    } else {
                        nextTrade = allTrades!![counterTrades]
                    }
                    counterTrades++
                }
            }
            while ((!flagTrans) and ((dateTimeFormatter(nextTransaction!!.dateTime) >= date.plusDays(
                    i.toLong()
                )) and (dateTimeFormatter(
                    nextTransaction.dateTime
                ) < date.plusDays((i + 1).toLong())))
            ) {
                if (!flagTrans) {
                    if ((dateTimeFormatter(nextTransaction.dateTime) >= date.plusDays(i.toLong())) and (dateTimeFormatter(
                            nextTransaction.dateTime
                        ) < date.plusDays((i + 1).toLong()))
                    ) {
                        if ((nextTransaction.currency == currency.toUpperCase()) and (nextTransaction.transactionStatus == "Complete")) {
                            when (nextTransaction.transactionType) {
                                "Deposit" -> {
                                    balanceAtStart.plus(nextTransaction.amount)
                                }
                                "Withdraw" -> {
                                    balanceAtStart.minus(nextTransaction.amount)
                                }
                            }
                        }

                    }

                }
                if (!flagTrans) {
                    if (allTransactions!!.size == counterTrans) {
                        flagTrans = true
                    } else {
                        nextTransaction = allTransactions!![counterTrans]
                    }
                }
                counterTrans++
            }
            dates.add(
                "${date.plusDays(i.toLong()).year}/${date.plusDays(i.toLong()).monthValue}/${date.plusDays(
                    i.toLong()
                ).dayOfMonth}"
            )
            rates.add(balanceAtStart)

        }


        var data: ArrayList<Rate>?
        var timeFrom =
            dateTimeFormatter("2014-11-08T00:00:00").atZone(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()!! / 1000
        var timeTo =
            dateTimeFormatter("2014-11-18T00:00:00").atZone(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()!! / 1000
        val httpAsync =
            "http://3.248.170.197:8888/bcv/quotes/bars/${currency}-usd/$timeFrom/$timeTo"
                .httpGet()
                .responseObject<ArrayList<Rate>> { _, _, result ->
                    when (result) {
                        is Result.Failure -> {
                            val ex = result.getException()
                            println(ex)
                        }
                        is Result.Success -> {
                            data = result.component1()
                            val formatter = SimpleDateFormat("dd/MM/yyyy")
                            if (!data.isNullOrEmpty())
                                for (i in 0..9) {
                                        if (data!!.size >= i + 1)
                                            rates[i] = rates[i] * data!![i].exchangeRate
                                }
                            aaChartModel = AAChartModel()
                                .chartType(AAChartType.Areaspline)
                                .title("Rate")
                                .titleFontColor("#0B1929")
                                .titleFontSize(20f)
                                .titleFontWeight(AAChartFontWeightType.Bold)
                                .subtitle("BTC to 10 days")
                                .subtitleFontColor("#0B1929")
                                .subtitleFontSize(15f)
                                .subtitleFontWeight(AAChartFontWeightType.Bold)
                                .marginright(10f)
                                .pointHollow(true)
                                .borderRadius(4f)
                                .axesTextColor("#0B1929")
                                .dataLabelsFontColor("#0B1929")
                                .dataLabelsFontSize(1f)
                                .xAxisTickInterval(2)
                                .yAxisTitle("USD")
                                //     .xAxisReversed(true)
                                .yAxisGridLineWidth(0.8f)
                                .xAxisGridLineWidth(0.8f)
                                .gradientColorEnable(true)
                                .markerRadius(4f)
                                .markerSymbolStyle(AAChartSymbolStyleType.InnerBlank)
                                .xAxisVisible(true)
                                .categories(dates.toTypedArray())
                                .legendEnabled(true)
                                .colorsTheme(arrayOf(AAGradientColor.berrySmoothieColor()))
                                .animationType(AAChartAnimationType.EaseInQuart)
                                .animationDuration(1200)
                                .series(
                                    arrayOf(
                                        AASeriesElement()
                                            .name("${currency.toUpperCase()}/USD")
                                            .data(rates.toArray())
                                    )
                                )

                            aaChartView?.aa_drawChartWithChartModel(aaChartModel!!)
                        }
                    }
                }

        httpAsync.join()


    }

    private fun modelingSeriesForIncome(currency: String) {
        var dates: ArrayList<String> = arrayListOf()
        var rates: ArrayList<BigDecimal> = arrayListOf()
        var timeNow = Calendar.getInstance().timeInMillis / 1000
        var time = timeNow - 13960000
        timeNow = time + 864000////10 days
        var data: ArrayList<Rate>?
        val httpAsync = "http://3.248.170.197:8888/bcv/quotes/bars/${currency}-usd/$time/$timeNow"
            .httpGet()
            .responseObject<ArrayList<Rate>> { _, _, result ->
                when (result) {
                    is Result.Failure -> {
                        val ex = result.getException()
                        println(ex)
                    }
                    is Result.Success -> {
                        data = result.component1()
                        var dataReversed = data!!.reversed()
                        var filtredTrades: ArrayList<Trade> = arrayListOf()
                        var filtredTrans: ArrayList<Transaction> = arrayListOf()
                        val formatter = SimpleDateFormat("dd/MM/yyyy")
                        if (!allTrades.isNullOrEmpty()) {
                            for (trade in allTrades!!) {
                                var timeTrade =
                                    dateTimeFormatter(trade.dateTime).atZone(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()!! / 1000
                                if (((trade.tradedPriceCurrency == currency.toUpperCase()) or (trade.tradedQuantityCurrency == currency.toUpperCase())) and (timeTrade >= time) and (timeTrade <= timeNow))
                                    filtredTrades.add(trade)
                            }
                        }
                        if (!allTransactions.isNullOrEmpty()) {
                            for (transaction in allTransactions!!) {
                                var timeTrans =
                                    dateTimeFormatter(transaction.dateTime).atZone(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()!! / 1000
                                if ((transaction.currency == currency.toUpperCase()) and (timeTrans >= time) and (timeTrans <= timeNow) and (transaction.transactionStatus == "Complete")) {
                                    var datte = formatter.format(timeTrans)
                                    filtredTrans.add(transaction)
                                }
                            }
                        }


                        for (i in 0..9) {
                            var income = BigDecimal(0)
                            for (trade in filtredTrades) {
                                if ((dateTimeFormatter(trade.dateTime).atZone(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()!! / 1000 >= time + 86400 * i)
                                    and (dateTimeFormatter(trade.dateTime).atZone(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()!! / 1000 < time + 86400 * (i + 1))
                                ) {
                                    when {
                                        (trade.instrument.startsWith(currency)) and (trade.tradeType == "Sell") -> {
                                            income -= trade.tradedQuantity * dataReversed[i].exchangeRate
                                        }
                                        (trade.instrument.startsWith(currency)) and (trade.tradeType == "Buy") -> {
                                            income += trade.tradedQuantity * dataReversed[i].exchangeRate
                                        }
                                        (trade.instrument.endsWith(currency)) and (trade.tradeType == "Buy") -> {
                                            income -= trade.tradedQuantity * trade.tradedPrice * dataReversed[i].exchangeRate
                                        }
                                        (trade.instrument.endsWith(currency)) and (trade.tradeType == "Sell") -> {
                                            income += trade.tradedQuantity * trade.tradedPrice * dataReversed[i].exchangeRate
                                        }
                                    }
                                }
                            }
                            for (transaction in filtredTrans) {
                                if ((dateTimeFormatter(transaction.dateTime).atZone(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()!! / 1000 >= time + 86400 * i)
                                    and (dateTimeFormatter(transaction.dateTime).atZone(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()!! / 1000 < time + 86400 * (i + 1))
                                ) {
                                    when {
                                        (transaction.transactionType == "Deposit") -> {
                                            income -= transaction.amount * dataReversed[i].exchangeRate
                                        }
                                        (transaction.transactionType == "Withdraw") -> {
                                            income += transaction.amount * dataReversed[i].exchangeRate
                                        }
                                    }
                                }
                            }
                            rates.add(income)
                            dates.add(formatter.format(time * 1000 + 86400000 * i))
                        }
                        aaChartModel = AAChartModel()
                            .chartType(AAChartType.Spline)
                            .title("Income")
                            .titleFontColor("#0B1929")
                            .titleFontSize(20f)
                            .titleFontWeight(AAChartFontWeightType.Bold)
                            .subtitle("BTC/USD")
                            .subtitleFontColor("#0B1929")
                            .subtitleFontSize(15f)
                            .subtitleFontWeight(AAChartFontWeightType.Bold)
                            .marginright(10f)
                            .pointHollow(true)
                            .borderRadius(4f)
                            .axesTextColor("#0B1929")
                            .dataLabelsFontColor("#0B1929")
                            .dataLabelsFontSize(1f)
                            .xAxisTickInterval(2)
                            .yAxisTitle("USD")
                            .yAxisGridLineWidth(0.8f)
                            .xAxisGridLineWidth(0.8f)
                            .gradientColorEnable(true)
                            .markerRadius(4f)
                            .markerSymbolStyle(AAChartSymbolStyleType.InnerBlank)
                            .xAxisVisible(true)
                            .categories(dates.toTypedArray())
                            .legendEnabled(true)
                            .colorsTheme(arrayOf(AAGradientColor.berrySmoothieColor()))
                            .animationType(AAChartAnimationType.EaseInQuart)
                            .animationDuration(1200)
                            .series(
                                arrayOf(
                                    AASeriesElement()
                                        .name("${currency.toUpperCase()}/USD")
                                        .data(rates.toArray())
                                )
                            )

                        aaChartView?.aa_drawChartWithChartModel(aaChartModel!!)
                    }
                }
            }

        httpAsync.join()
    }

    private fun drawGraphInputOutput(inOut: Pair<Array<Any>, Array<Any>>) {
        val input = inOut.first
        val output = inOut.second
        aaChartModel = AAChartModel()
            .chartType(AAChartType.Bar)
            .title("Input/Output")
            .titleFontColor("#0B1929")
            .titleFontSize(20f)
            .titleFontWeight(AAChartFontWeightType.Bold)
            .subtitleFontColor("#0B1929")
            .axesTextColor("#0B1929")
            .dataLabelsFontColor("#0B1929")
            .yAxisTitle("Dollars")
            .subtitleFontWeight(AAChartFontWeightType.Bold)
            .categories(
                arrayOf(
                    "Jan",
                    "Feb",
                    "Mar",
                    "Apr",
                    "May",
                    "Jun",
                    "Jul",
                    "Aug",
                    "Sep",
                    "Oct",
                    "Nov",
                    "Dec"
                )
            )
            .legendEnabled(true)
            .colorsTheme(
                arrayOf(
                    AAGradientColor.berrySmoothieColor(),
                    AAGradientColor.oceanBlueColor()
                )
            )
            .animationType(AAChartAnimationType.EaseInQuart)
            .xAxisReversed(true)
            .animationDuration(1200)
            .series(
                arrayOf(
                    AASeriesElement()
                        .name("Input $")
                        .data(input),
                    AASeriesElement()
                        .name("Output $")
                        .data(output)
                )
            )
        aaChartView?.aa_drawChartWithChartModel(aaChartModel!!)
    }


    private fun modelingSeriesForRate(currency: String) {
        var dates: ArrayList<String> = arrayListOf()
        var rates: ArrayList<BigDecimal> = arrayListOf()
        var data: ArrayList<Rate>?
        var timeNow = Calendar.getInstance().timeInMillis / 1000
        var time = timeNow - 2628000
        val httpAsync = "http://3.248.170.197:8888/bcv/quotes/bars/${currency}-usd/$time/$timeNow"
            .httpGet()
            .responseObject<ArrayList<Rate>> { _, _, result ->
                when (result) {
                    is Result.Failure -> {
                        val ex = result.getException()
                        println(ex)
                    }
                    is Result.Success -> {
                        data = result.component1()
                        val formatter = SimpleDateFormat("dd/MM/yyyy")
                        if (!data.isNullOrEmpty())
                            for (i in data!!) {
                                rates.add(i.exchangeRate)
                                var date = formatter.format(i.timestamp)
                                dates.add(date)
                            }
                        aaChartModel = AAChartModel()
                            .chartType(AAChartType.Areaspline)
                            .title("Rate")
                            .titleFontColor("#0B1929")
                            .titleFontSize(20f)
                            .titleFontWeight(AAChartFontWeightType.Bold)
                            .subtitle("BTC/USD")
                            .subtitleFontColor("#0B1929")
                            .subtitleFontSize(15f)
                            .subtitleFontWeight(AAChartFontWeightType.Bold)
                            .marginright(10f)
                            .pointHollow(true)
                            .borderRadius(4f)
                            .axesTextColor("#0B1929")
                            .dataLabelsFontColor("#0B1929")
                            .dataLabelsFontSize(1f)
                            .xAxisTickInterval(2)
                            .yAxisTitle("USD")
                            //     .xAxisReversed(true)
                            .yAxisGridLineWidth(0.8f)
                            .xAxisGridLineWidth(0.8f)
                            .gradientColorEnable(true)
                            .markerRadius(4f)
                            .markerSymbolStyle(AAChartSymbolStyleType.InnerBlank)
                            .xAxisVisible(true)
                            .categories(dates.toTypedArray())
                            .legendEnabled(true)
                            .colorsTheme(arrayOf(AAGradientColor.berrySmoothieColor()))
                            .animationType(AAChartAnimationType.EaseInQuart)
                            .animationDuration(1200)
                            .series(
                                arrayOf(
                                    AASeriesElement()
                                        .name("${currency.toUpperCase()}/USD")
                                        .data(rates.toArray())
                                )
                            )

                        aaChartView?.aa_drawChartWithChartModel(aaChartModel!!)
                    }
                }
            }

        httpAsync.join()

    }


    private fun modelingSeriesForInputOutput(
        transactions: ArrayList<Transaction>?,
        year: Int
    ): Pair<Array<Any>, Array<Any>> {
        var transaction: Transaction? = null
        var index = 0
        var flagSearchFirst =
            -1                                                                                                                     //-1 -> there is no transactions at all
        if (transactions != null) {
            transaction = transactions[0]
            flagSearchFirst =
                0                                                                                                                 //0 -> there is some transactions
        }
        val resultInput: ArrayList<BigDecimal> = arrayListOf()
        val resultOutput: ArrayList<BigDecimal> = arrayListOf()
        for (i in 0..11) {
            resultInput.add(BigDecimal(0))
            resultOutput.add(BigDecimal(0))
        }
        while (flagSearchFirst == 0) {
            when {
                dateTimeFormatter(transaction!!.dateTime).year == year -> flagSearchFirst =
                    1                                                 //1 -> it is first transaction on this year
                transactions!!.last().transactionValueId == transaction.transactionValueId -> flagSearchFirst =
                    -2                                                                                                            //-2 -> there is no transactions on this year
                else -> {
                    index++
                    transaction = transactions[index]
                }
            }
        }
        if (flagSearchFirst == 1) {
            while ((dateTimeFormatter(transaction!!.dateTime).year == year) and (index < transactions!!.size)) {
                if (transaction.transactionStatus == "Complete") {
                    if (transaction.transactionType == "Deposit") {
                        resultInput[dateTimeFormatter(transaction.dateTime).monthValue - 1] += transaction.amount
                    } else {
                        resultOutput[dateTimeFormatter(transaction.dateTime).monthValue - 1] += transaction.amount
                    }
                }
                index++
                if (index < transactions.size)
                    transaction = transactions[index]
            }
        }
        return Pair(resultInput.toArray(), resultOutput.toArray())
    }


    private fun modelingSeriesForPie(balances: MutableMap<String, BigDecimal?>) {
        var dats: ArrayList<String> = ArrayList()
        for (key in balances.keys) {
            if ((key != "TRY") and (key != "BCH"))
                dats.add(key)
        }
        val resultF: ArrayList<Any> = arrayListOf()
        val dat: ArrayList<BigDecimal?> = arrayListOf()
        val dat2: ArrayList<String> = arrayListOf()
        var counter = 0
        for (balance in balances) {
            var rate: BigDecimal

            val httpAsync =
                "http://3.248.170.197:8888/bcv/quotes/ticker/${balance.key.toLowerCase()}-usd"
                    .httpGet()
                    .responseObject<Tiker> { _, _, result ->
                        when (result) {
                            is Result.Failure -> {
                                val ex = result.getException()
                                println(ex)
                            }
                            is Result.Success -> {
                                counter++
                                rate = result.component1()!!.exchangeRate
                                if (balance.value != null)
                                    dat.add(balance.value!! * rate)
                                else dat.add(BigDecimal(0.0))
                                dat2.add(balance.key)

                                if (counter == balances.size - 2) {
                                    for (i in 0 until dat2.size) {
                                        resultF.add(arrayOf(dat2[i], dat[i]!!))
                                    }
                                    aaChartModel = AAChartModel()
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
                                        .categories(dats.toTypedArray())
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


                                    aaChartView?.aa_drawChartWithChartModel(aaChartModel!!)
                                }
                            }
                        }
                    }

            httpAsync.join()
        }

    }

    private fun modelingSeriesForGraph(year: Int): Array<AASeriesElement> {
        val result: ArrayList<AASeriesElement> = arrayListOf()
        val currencies: LinkedList<String> = LinkedList()
        var dataStart: LocalDateTime
        var dataEnd: LocalDateTime = dateTimeFormatter("${year}-02-01T00:00:00")
        var balanceForMonth = balanceForDateOnlyTrades(dataEnd)
        for (key in balanceForMonth.keys)
            currencies.add(key)
        val yearBalance: ArrayList<MutableMap<String, BigDecimal?>> = arrayListOf()
        for (i in 0..12)
            yearBalance.add(mutableMapOf())
        for (cur in balanceForMonth.keys) {
            yearBalance[1][cur] = balanceForMonth.getValue(cur)
        }
        for (m in 2..12) {
            dataStart = if (m < 10)
                dateTimeFormatter("${year}-0${m}-01T00:00:00")
            else
                dateTimeFormatter("${year}-${m}-01T00:00:00")
            dataEnd = when {
                m < 9 -> dateTimeFormatter("${year}-0${m + 1}-01T00:00:00")
                m != 12 -> dateTimeFormatter("${year}-${m + 1}-01T00:00:00")
                else -> dateTimeFormatter("${year + 1}-01-01T00:00:00")
            }
            balanceForMonth = balanceFromDateToDateOnlyTrades(dataStart, dataEnd)
            for (key in balanceForMonth.keys)
                if (!currencies.contains(key))
                    currencies.add(key)
            for (cur in yearBalance[m - 1].keys)
                yearBalance[m][cur] = yearBalance[m - 1].getValue(cur)!!
            for (key in balanceForMonth.keys) {
                if (yearBalance[m].keys.contains(key))
                    yearBalance[m][key] = yearBalance[m][key]?.plus(balanceForMonth[key]!!)
                else yearBalance[m][key] = balanceForMonth[key]

            }
        }


        for (key in currencies) {
            val dat: ArrayList<BigDecimal?> = arrayListOf()
            for (m in 1..12) {
                if (yearBalance[m].containsKey(key))
                    dat.add(yearBalance[m].getValue(key)!!)
                else
                    dat.add(BigDecimal(0))
            }
            result.add(
                AASeriesElement()
                    .name(key)
                    .data(dat.toArray())
                    .stack("1")
            )
        }
        return result.toTypedArray()
    }


    private fun modelingSeriesForIncomePortfolio(balances: MutableMap<String, BigDecimal?>) {
        var dates: ArrayList<String> = arrayListOf()
        var rates: ArrayList<BigDecimal> = arrayListOf()
        var timeFrom =
            dateTimeFormatter("2019-09-20T00:00:00").atZone(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()!! / 1000
        var time = timeFrom + 864000
        var data: ArrayList<Rate>?
        var counter = 0
        for (balance in balances) {


            val httpAsync =
                "http://3.248.170.197:8888/bcv/quotes/bars/${balance.key.toLowerCase()}-usd/$timeFrom/$time"
                    .httpGet()
                    .responseObject<ArrayList<Rate>> { _, _, result ->
                        when (result) {
                            is Result.Failure -> {
                                val ex = result.getException()
                                println(ex)
                            }
                            is Result.Success -> {
                                counter++
                                data = result.component1()
                                //var dataReversed = data!!.reversed()
                                var filtredTrades: ArrayList<Trade> = arrayListOf()
                                var filtredTrans: ArrayList<Transaction> = arrayListOf()
                                val formatter = SimpleDateFormat("dd/MM/yyyy")
                                if (!allTrades.isNullOrEmpty()) {
                                    for (trade in allTrades!!) {
                                        var timeTrade =
                                            dateTimeFormatter(trade.dateTime).atZone(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()!! / 1000

                                        if (((trade.tradedPriceCurrency == balance.key) or (trade.tradedQuantityCurrency == balance.key)) and (timeTrade >= timeFrom) and (timeTrade <= time))
                                            filtredTrades.add(trade)
                                    }
                                }
                                if (!allTransactions.isNullOrEmpty()) {

                                    for (transaction in allTransactions!!) {
                                        var timeTrans =
                                            dateTimeFormatter(transaction.dateTime).atZone(
                                                ZoneOffset.UTC
                                            )?.toInstant()?.toEpochMilli()!! / 1000
                                        if ((transaction.currency == balance.key) and (timeTrans >= timeFrom) and (timeTrans <= time) and (transaction.transactionStatus == "Complete")) {
                                            var datte = formatter.format(timeTrans)
                                            filtredTrans.add(transaction)
                                        }
                                    }
                                }


                                for (i in 0..9) {
                                    var income = BigDecimal(0)
                                    for (trade in filtredTrades) {
                                        if ((dateTimeFormatter(trade.dateTime).atZone(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()!! / 1000 >= timeFrom + 86400 * i)
                                            and (dateTimeFormatter(trade.dateTime).atZone(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()!! / 1000 < timeFrom + 86400 * (i + 1))
                                        ) {
                                            when {
                                                (trade.tradedQuantityCurrency == balance.key) and (trade.tradeType == "Sell") -> {
                                                    income -= trade.tradedQuantity * data!![i].exchangeRate
                                                }
                                                (trade.tradedQuantityCurrency == balance.key) and (trade.tradeType == "Buy") -> {
                                                    income += trade.tradedQuantity * data!![i].exchangeRate
                                                }
                                                (trade.instrument.endsWith(balance.key.toLowerCase())) and (trade.tradeType == "Buy") -> {
                                                    income -= trade.tradedQuantity * trade.tradedPrice * data!![i].exchangeRate
                                                }
                                                (trade.instrument.endsWith(balance.key.toLowerCase())) and (trade.tradeType == "Sell") -> {
                                                    income += trade.tradedQuantity * trade.tradedPrice * data!![i].exchangeRate
                                                }
                                            }
                                        }
                                    }
                                    for (transaction in filtredTrans) {
                                        if ((dateTimeFormatter(transaction.dateTime).atZone(
                                                ZoneOffset.UTC
                                            )?.toInstant()?.toEpochMilli()!! / 1000 >= timeFrom + 86400 * i)
                                            and (dateTimeFormatter(transaction.dateTime).atZone(
                                                ZoneOffset.UTC
                                            )?.toInstant()?.toEpochMilli()!! / 1000 < timeFrom + 86400 * (i + 1))
                                        ) {
                                            when {
                                                (transaction.transactionType == "Deposit") -> {
                                                    income -= transaction.amount * data!![i].exchangeRate
                                                }
                                                (transaction.transactionType == "Withdraw") -> {
                                                    income += transaction.amount * data!![i].exchangeRate
                                                }
                                            }
                                        }
                                    }
                                    if (counter == 1) {
                                        rates.add(income)
                                        dates.add(formatter.format(timeFrom * 1000 + 86400000 * i))
                                    } else {
                                        rates[i] += income
                                    }
                                }
                                if (counter == balances.size - 2) {
                                    aaChartModel = AAChartModel()
                                        .chartType(AAChartType.Spline)
                                        .title("Portfolio income in $")
                                        .titleFontColor("#0B1929")
                                        .titleFontSize(20f)
                                        .titleFontWeight(AAChartFontWeightType.Bold)
                                        .subtitleFontColor("#0B1929")
                                        .subtitleFontSize(15f)
                                        .subtitleFontWeight(AAChartFontWeightType.Bold)
                                        .marginright(10f)
                                        .pointHollow(true)
                                        .borderRadius(4f)
                                        .axesTextColor("#0B1929")
                                        .dataLabelsFontColor("#0B1929")
                                        .dataLabelsFontSize(1f)
                                        .xAxisTickInterval(2)
                                        .yAxisTitle("USD")
                                        .yAxisGridLineWidth(0.8f)
                                        .xAxisGridLineWidth(0.8f)
                                        .gradientColorEnable(true)
                                        .markerRadius(4f)
                                        .markerSymbolStyle(AAChartSymbolStyleType.InnerBlank)
                                        .xAxisVisible(true)
                                        .categories(dates.toTypedArray())
                                        .legendEnabled(true)
                                        .colorsTheme(arrayOf(AAGradientColor.berrySmoothieColor()))
                                        .animationType(AAChartAnimationType.EaseInQuart)
                                        .animationDuration(1200)
                                        .series(
                                            arrayOf(
                                                AASeriesElement()
                                                    .name("Income in $")
                                                    .data(rates.toArray())
                                            )
                                        )

                                    aaChartView?.aa_drawChartWithChartModel(aaChartModel!!)
                                }
                            }
                        }
                    }

            httpAsync.join()
        }
    }

    private fun balanceForDate(date: LocalDateTime): MutableMap<String, BigDecimal?> {
        val result: MutableMap<String, BigDecimal?> = mutableMapOf()
        var flagTrades = false
        var flagTransactions = false
        var nextTrade: Trade? = null
        var nextTransaction: Transaction? = null
        if (allTrades == null) {
            flagTrades = true
        } else {
            nextTrade = allTrades!![0]
        }
        if (allTransactions == null) {
            flagTransactions = true
        } else {
            nextTransaction = allTransactions!![0]
        }

        var i = 1
        while ((!flagTrades) or (!flagTransactions)) {

            if (!flagTrades) {
                if (dateTimeFormatter(nextTrade!!.dateTime) > date) {
                    flagTrades = true
                } else {
                    if (!result.keys.contains(nextTrade.tradedQuantityCurrency))
                        result[nextTrade.tradedQuantityCurrency] = BigDecimal(0.0)
                    if (!result.keys.contains(nextTrade.tradedPriceCurrency))
                        result[nextTrade.tradedPriceCurrency] = BigDecimal(0.0)

                    when (nextTrade.tradeType) {
                        ("Sell") -> {
                            result[nextTrade.tradedQuantityCurrency] =
                                result[nextTrade.tradedQuantityCurrency]?.minus(nextTrade.tradedQuantity)
                            result[nextTrade.tradedPriceCurrency] =
                                result[nextTrade.tradedPriceCurrency]?.plus(nextTrade.tradedPrice * nextTrade.tradedQuantity - nextTrade.commission)
                        }
                        ("Buy") -> {
                            result[nextTrade.tradedQuantityCurrency] =
                                result[nextTrade.tradedQuantityCurrency]?.plus(nextTrade.tradedQuantity - nextTrade.commission)
                            result[nextTrade.tradedPriceCurrency] =
                                result[nextTrade.tradedPriceCurrency]?.minus(nextTrade.tradedPrice * nextTrade.tradedQuantity)
                        }
                    }
                }
            }


            if (!flagTransactions) {
                if (dateTimeFormatter(nextTransaction!!.dateTime) > date) {
                    flagTransactions = true
                } else {

                    if (!result.keys.contains(nextTransaction.currency))
                        result[nextTransaction.currency] = BigDecimal(0.0)
                    if (nextTransaction.transactionStatus == "Complete") {
                        if (nextTransaction.transactionType == "Deposit")
                            result[nextTransaction.currency] =
                                result[nextTransaction.currency]?.plus(nextTransaction.amount - nextTransaction.commission)
                        else {
                            result[nextTransaction.currency] =
                                result[nextTransaction.currency]?.minus(nextTransaction.amount + nextTransaction.commission)
                        }
                    }
                }
            }

            if (!flagTrades) {
                if (allTrades!!.size == i) {
                    flagTrades = true
                } else {
                    nextTrade = allTrades!![i]
                }
            }
            if (!flagTransactions) {
                if (allTransactions!!.size == i) {
                    flagTransactions = true
                } else {
                    nextTransaction = allTransactions!![i]
                }
            }
            i++
        }
        return result
    }

    private fun balanceForDateOneCurr(date: LocalDateTime, currency: String): BigDecimal {
        var result: BigDecimal = BigDecimal(0)
        var flagTrades = false
        var flagTransactions = false
        var nextTrade: Trade? = null
        var nextTransaction: Transaction? = null
        if (allTrades == null) {
            flagTrades = true
        } else {
            nextTrade = allTrades!![0]
        }
        if (allTransactions == null) {
            flagTransactions = true
        } else {
            nextTransaction = allTransactions!![0]
        }

        var i = 1
        while ((!flagTrades) or (!flagTransactions)) {

            if (!flagTrades) {
                if (dateTimeFormatter(nextTrade!!.dateTime) > date) {
                    flagTrades = true
                } else {
                    if (nextTrade.tradedQuantityCurrency == currency) {
                        when (nextTrade.tradeType) {
                            ("Sell") -> {
                                result =
                                    result.minus(nextTrade.tradedQuantity)
                            }
                            ("Buy") -> {
                                result =
                                    result.plus(nextTrade.tradedQuantity - nextTrade.commission)
                            }
                        }
                    }
                    if (nextTrade.tradedPriceCurrency == currency) {
                        when (nextTrade.tradeType) {
                            ("Sell") -> {
                                result =
                                    result.plus(nextTrade.tradedQuantity * nextTrade.tradedPrice - nextTrade.commission)
                            }
                            ("Buy") -> {
                                result =
                                    result.minus(nextTrade.tradedQuantity * nextTrade.tradedPrice)
                            }
                        }
                    }

                }
            }

            if (!flagTransactions) {
                if (dateTimeFormatter(nextTransaction!!.dateTime) > date) {
                    flagTransactions = true
                } else {
                    if (nextTransaction.currency == currency) {
                        if (nextTransaction.transactionStatus == "Complete") {
                            result = if (nextTransaction.transactionType == "Deposit")
                                result.plus(nextTransaction.amount - nextTransaction.commission)
                            else {
                                result.minus(nextTransaction.amount + nextTransaction.commission)
                            }
                        }
                    }
                }
            }

            if (!flagTrades) {
                if (allTrades!!.size == i) {
                    flagTrades = true
                } else {
                    nextTrade = allTrades!![i]
                }
            }
            if (!flagTransactions) {
                if (allTransactions!!.size == i) {
                    flagTransactions = true
                } else {
                    nextTransaction = allTransactions!![i]
                }
            }
            i++
        }
        return result
    }

    private fun balanceFromDateToDate(
        dateStart: LocalDateTime,
        dateEnd: LocalDateTime
    ): MutableMap<String, BigDecimal?> {
        val result: MutableMap<String, BigDecimal?> = mutableMapOf()
        var flagTrades = false
        var flagTransactions = false
        var nextTrade: Trade? = null
        var nextTransaction: Transaction? = null
        if (allTrades == null) {
            flagTrades = true
        } else {
            nextTrade = allTrades!![0]
        }
        if (allTransactions == null) {
            flagTransactions = true
        } else {
            nextTransaction = allTransactions!![0]
        }

        var i = 1
        while ((!flagTrades) or (!flagTransactions)) {

            if (!flagTrades) {
                if (dateTimeFormatter(nextTrade!!.dateTime) > dateEnd) {
                    flagTrades = true
                } else {
                    if (dateTimeFormatter(nextTrade.dateTime) >= dateStart) {
                        if (!result.keys.contains(nextTrade.tradedQuantityCurrency))
                            result[nextTrade.tradedQuantityCurrency] = BigDecimal(0.0)
                        if (!result.keys.contains(nextTrade.tradedPriceCurrency))
                            result[nextTrade.tradedPriceCurrency] = BigDecimal(0.0)
                        when (nextTrade.tradeType) {
                            ("Sell") -> {
                                result[nextTrade.tradedQuantityCurrency] =
                                    result[nextTrade.tradedQuantityCurrency]?.minus(nextTrade.tradedQuantity)
                                result[nextTrade.tradedPriceCurrency] =
                                    result[nextTrade.tradedPriceCurrency]?.plus(nextTrade.tradedPrice * nextTrade.tradedQuantity - nextTrade.commission)
                            }
                            ("Buy") -> {
                                result[nextTrade.tradedQuantityCurrency] =
                                    result[nextTrade.tradedQuantityCurrency]?.plus(nextTrade.tradedQuantity - nextTrade.commission)
                                result[nextTrade.tradedPriceCurrency] =
                                    result[nextTrade.tradedPriceCurrency]?.minus(nextTrade.tradedPrice * nextTrade.tradedQuantity)
                            }
                        }
                    }
                }
            }

            if (!flagTransactions) {
                if (dateTimeFormatter(nextTransaction!!.dateTime) > dateEnd) {
                    flagTransactions = true
                } else {
                    if (dateTimeFormatter(nextTransaction.dateTime) >= dateStart) {
                        if (!result.keys.contains(nextTransaction.currency))
                            result[nextTransaction.currency] = BigDecimal(0.0)
                        if (nextTransaction.transactionStatus == "Complete") {
                            if (nextTransaction.transactionType == "Deposit")
                                result[nextTransaction.currency] =
                                    result[nextTransaction.currency]?.plus(nextTransaction.amount - nextTransaction.commission)
                            else {
                                result[nextTransaction.currency] =
                                    result[nextTransaction.currency]?.minus(nextTransaction.amount + nextTransaction.commission)
                            }
                        }
                    }
                }
            }

            if (!flagTrades) {
                if (allTrades!!.size == i) {
                    flagTrades = true
                } else {
                    nextTrade = allTrades!![i]
                }
            }
            if (!flagTransactions) {
                if (allTransactions!!.size == i) {
                    flagTransactions = true
                } else {
                    nextTransaction = allTransactions!![i]
                }
            }
            i++
        }
        return result
    }

    private fun dateTimeFormatter(string: String): LocalDateTime {
        return LocalDateTime.parse(string, DateTimeFormatter.ISO_DATE_TIME)
    }


    private fun balanceForDateOnlyTrades(date: LocalDateTime): MutableMap<String, BigDecimal?> {
        val result: MutableMap<String, BigDecimal?> = mutableMapOf()
        var flagTrades = false
        var nextTrade: Trade? = null
        if (allTrades == null) {
            flagTrades = true
        } else {
            nextTrade = allTrades!![0]
        }

        var i = 1
        while (!flagTrades) {

            if (!flagTrades) {
                if (dateTimeFormatter(nextTrade!!.dateTime) > date) {
                    flagTrades = true
                } else {
                    if (!result.keys.contains(nextTrade.tradedQuantityCurrency))
                        result[nextTrade.tradedQuantityCurrency] = BigDecimal(0.0)
                    if (!result.keys.contains(nextTrade.tradedPriceCurrency))
                        result[nextTrade.tradedPriceCurrency] = BigDecimal(0.0)

                    when (nextTrade.tradeType) {
                        ("Sell") -> {
                            result[nextTrade.tradedQuantityCurrency] =
                                result[nextTrade.tradedQuantityCurrency]?.minus(nextTrade.tradedQuantity)
                            if (result[nextTrade.tradedQuantityCurrency]!! < BigDecimal(0)) {
                                allAddedTransactions?.add(
                                    Transaction(
                                        amount = -result[nextTrade.tradedQuantityCurrency]!!,
                                        commission = BigDecimal(0),
                                        currency = nextTrade.tradedQuantityCurrency,
                                        dateTime = nextTrade.dateTime,
                                        id = idForAddTrans,
                                        transactionStatus = "Complete",
                                        transactionType = "Deposit",
                                        transactionValueId = idForAddTrans.toString()
                                    )
                                )
                                idForAddTrans++
                                result[nextTrade.tradedQuantityCurrency] = BigDecimal(0)
                            }
                            result[nextTrade.tradedPriceCurrency] =
                                result[nextTrade.tradedPriceCurrency]?.plus(nextTrade.tradedPrice * nextTrade.tradedQuantity - nextTrade.commission)
                        }
                        ("Buy") -> {
                            result[nextTrade.tradedQuantityCurrency] =
                                result[nextTrade.tradedQuantityCurrency]?.plus(nextTrade.tradedQuantity - nextTrade.commission)
                            result[nextTrade.tradedPriceCurrency] =
                                result[nextTrade.tradedPriceCurrency]?.minus(nextTrade.tradedPrice * nextTrade.tradedQuantity)
                            if (result[nextTrade.tradedPriceCurrency]!! < BigDecimal(0)) {
                                allAddedTransactions?.add(
                                    Transaction(
                                        amount = -result[nextTrade.tradedPriceCurrency]!!,
                                        commission = BigDecimal(0),
                                        currency = nextTrade.tradedPriceCurrency,
                                        dateTime = nextTrade.dateTime,
                                        id = idForAddTrans,
                                        transactionStatus = "Complete",
                                        transactionType = "Deposit",
                                        transactionValueId = idForAddTrans.toString()
                                    )
                                )
                                idForAddTrans++
                                result[nextTrade.tradedPriceCurrency] = BigDecimal(0)
                            }
                        }
                    }
                }
            }

            if (!flagTrades) {
                if (allTrades!!.size == i) {
                    flagTrades = true
                } else {
                    nextTrade = allTrades!![i]
                }
            }

            i++
        }
        return result
    }

    private fun balanceFromDateToDateOnlyTrades(
        dateStart: LocalDateTime,
        dateEnd: LocalDateTime
    ): MutableMap<String, BigDecimal?> {
        val result: MutableMap<String, BigDecimal?> = mutableMapOf()
        var flagTrades = false
        var nextTrade: Trade? = null
        if (allTrades == null) {
            flagTrades = true
        } else {
            nextTrade = allTrades!![0]
        }

        var i = 1
        while (!flagTrades) {

            if (!flagTrades) {
                if (dateTimeFormatter(nextTrade!!.dateTime) > dateEnd) {
                    flagTrades = true
                } else {
                    if (dateTimeFormatter(nextTrade.dateTime) >= dateStart) {
                        if (!result.keys.contains(nextTrade.tradedQuantityCurrency))
                            result[nextTrade.tradedQuantityCurrency] = BigDecimal(0.0)
                        if (!result.keys.contains(nextTrade.tradedPriceCurrency))
                            result[nextTrade.tradedPriceCurrency] = BigDecimal(0.0)
                        when (nextTrade.tradeType) {
                            ("Sell") -> {
                                result[nextTrade.tradedQuantityCurrency] =
                                    result[nextTrade.tradedQuantityCurrency]?.minus(nextTrade.tradedQuantity)
                                if (result[nextTrade.tradedQuantityCurrency]!! < BigDecimal(0)) {
                                    allAddedTransactions?.add(
                                        Transaction(
                                            amount = -result[nextTrade.tradedQuantityCurrency]!!,
                                            commission = BigDecimal(0),
                                            currency = nextTrade.tradedQuantityCurrency,
                                            dateTime = nextTrade.dateTime,
                                            id = idForAddTrans,
                                            transactionStatus = "Complete",
                                            transactionType = "Deposit",
                                            transactionValueId = idForAddTrans.toString()
                                        )
                                    )
                                    idForAddTrans++
                                    result[nextTrade.tradedQuantityCurrency] = BigDecimal(0)
                                }
                                result[nextTrade.tradedPriceCurrency] =
                                    result[nextTrade.tradedPriceCurrency]?.plus(nextTrade.tradedPrice * nextTrade.tradedQuantity - nextTrade.commission)
                            }
                            ("Buy") -> {
                                result[nextTrade.tradedQuantityCurrency] =
                                    result[nextTrade.tradedQuantityCurrency]?.plus(nextTrade.tradedQuantity - nextTrade.commission)
                                result[nextTrade.tradedPriceCurrency] =
                                    result[nextTrade.tradedPriceCurrency]?.minus(nextTrade.tradedPrice * nextTrade.tradedQuantity)
                                if (result[nextTrade.tradedPriceCurrency]!! < BigDecimal(0)) {
                                    allAddedTransactions?.add(
                                        Transaction(
                                            amount = -result[nextTrade.tradedPriceCurrency]!!,
                                            commission = BigDecimal(0),
                                            currency = nextTrade.tradedPriceCurrency,
                                            dateTime = nextTrade.dateTime,
                                            id = idForAddTrans,
                                            transactionStatus = "Complete",
                                            transactionType = "Deposit",
                                            transactionValueId = idForAddTrans.toString()
                                        )
                                    )
                                    idForAddTrans++
                                    result[nextTrade.tradedPriceCurrency] = BigDecimal(0)
                                }
                            }
                        }
                    }
                }
            }

            if (!flagTrades) {
                if (allTrades!!.size == i) {
                    flagTrades = true
                } else {
                    nextTrade = allTrades!![i]
                }
            }
            i++
        }
        return result
    }


}