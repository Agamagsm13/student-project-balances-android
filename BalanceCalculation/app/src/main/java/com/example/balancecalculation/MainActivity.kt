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
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {


    var isMenu1Pressed = false                                                                         //is there pressed popup menu on the right corner
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
    private var allAddedTransactions: ArrayList<Transaction>? = arrayListOf()                          //transactions that added artificially for positive balances
    var showYearsButton: Button? = null
    private var years: ArrayList<Int> = arrayListOf()
    private var yearForSeriesGraph: Int = 2014
    private var isDownloaded = false                                                                   //flag that client already downloaded his trades and transactions
    private var idForAddTrans = 0
    private var aaChartView: AAChartView? = null
    private var aaChartModel: AAChartModel? = null
    var finalBalances = mutableMapOf<String, BigDecimal?>()                                            //balances after all trades and transactions


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        aaChartView = findViewById(R.id.AAChartView)
        showYearsButton = findViewById(R.id.PopupButton)

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun start() {
        val urlTrades = "http://3.248.170.197:9999/bcv/trades"
        val urlTransactions = "http://3.248.170.197:9999/bcv/transactions"
        aaChartView!!.setOnTouchListener(object : OnSwipeTouchListener() {
            override fun onSwipeLeft() {
                counterForGraphs = (counterForGraphs + 1) % 10
                when (counterForGraphs) {
                    1 -> {
                        drawGraphPie(finalBalances)
                        showYearsButton?.visibility = View.INVISIBLE
                    }
                    2 -> {
                        drawGraphInputOutput(allTransactions, 2014)
                        showYearsButton?.visibility = View.VISIBLE
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
                        drawGraphPie(finalBalances)
                        showYearsButton?.visibility = View.INVISIBLE
                    }
                }
            }
        })

        async {
            if (!isDownloaded) {
                allTransactions = await {
                    urlTransactions.httpGet().responseObject<ArrayList<Transaction>>().third.get()
                }
                allTrades =
                    await { urlTrades.httpGet().responseObject<ArrayList<Trade>>().third.get() }

                showYearsButton?.visibility = View.VISIBLE
                if (allTrades != null) {
                    allTrades!!.sortBy { it.dateTime }
                    years.add(dateTimeFormmatter(allTrades!!.first().dateTime).year)
                    if (!years.contains(dateTimeFormmatter(allTrades!!.last().dateTime).year))
                        years.add(dateTimeFormmatter(allTrades!!.last().dateTime).year)
                }
                if (allTransactions != null) {
                    allTransactions!!.sortBy { it.dateTime }
                    if (!years.contains(dateTimeFormmatter(allTransactions!!.last().dateTime).year))
                        years.add(dateTimeFormmatter(allTransactions!!.last().dateTime).year)
                    if (!years.contains(dateTimeFormmatter(allTransactions!!.first().dateTime).year))
                        years.add(dateTimeFormmatter(allTransactions!!.first().dateTime).year)
                }
                var date: LocalDateTime

                if ((allTransactions != null) and (allTrades != null)) {
                    date = dateTimeFormmatter(allTrades!!.last().dateTime)
                    if (dateTimeFormmatter(allTransactions!!.last().dateTime) > date) {
                        date = dateTimeFormmatter(allTransactions!!.last().dateTime)
                    }
                } else
                    date = dateTimeFormmatter("2014-12-31T23:59:59")
                finalBalances = balanceForDateOnlyTrades(date)
                for (i in (years.min()!!..years.max()!!)) {
                    if (!years.contains(i))
                        years.add(i)
                }
                years.sort()
                isDownloaded = true
            }
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
                        drawGraphColumn()
                    }
                    if (counterForGraphs == 2) {
                        drawGraphInputOutput(allTransactions, item.title.toString().toInt())
                    }
                    true
                }
                popupMenu!!.show()

            }

            drawGraphColumn()
        }
    }


    private fun drawGraphColumn() {

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
            .series(modelingSeriesForGraph(yearForSeriesGraph))

        aaChartView?.aa_drawChartWithChartModel(aaChartModel!!)


    }

    private fun drawGraphPie(balances: MutableMap<String, BigDecimal?>) {
        val dats: ArrayList<String> = arrayListOf()
        for (key in balances.keys) {
            dats.add(key)
        }
        val data = modelingSeriesForPie(balances)
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
                            data
                        )
                )
            )


        aaChartView?.aa_drawChartWithChartModel(aaChartModel!!)


    }

    private fun drawGraphInputOutput(transactions: ArrayList<Transaction>?, year: Int) {
        val input = modelingSeriesForInputOutput(transactions, year).first
        val output = modelingSeriesForInputOutput(transactions, year).second
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


    private fun modelingSeriesForInputOutput(
        transactions: ArrayList<Transaction>?,
        year: Int
    ): Pair<Array<Any>, Array<Any>> {
        var transaction: Transaction? = null
        var index = 0
        var flagSearchFirst =
            -1                                                                                                                //-1 -> there is no transactions at all
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
                dateTimeFormmatter(transaction!!.dateTime).year == year -> flagSearchFirst =
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
            while ((dateTimeFormmatter(transaction!!.dateTime).year == year) and (index < transactions!!.size)) {
                if (transaction.transactionStatus == "Complete") {
                    if (transaction.transactionType == "Deposit") {
                        resultInput[dateTimeFormmatter(transaction.dateTime).monthValue - 1] += transaction.amount
                    } else {
                        resultOutput[dateTimeFormmatter(transaction.dateTime).monthValue - 1] += transaction.amount
                    }
                }
                index++
                if (index < transactions.size)
                    transaction = transactions[index]
            }
        }
        return Pair(resultInput.toArray(), resultOutput.toArray())
    }

    private fun modelingSeriesForPie(balances: MutableMap<String, BigDecimal?>): Array<Any> {
        val result: ArrayList<Any> = arrayListOf()
        val dat: ArrayList<BigDecimal?> = arrayListOf()
        for (value in balances.values) {
            if (value != null)
                dat.add(value)
            else dat.add(BigDecimal(0.0))
        }
        val dat2: ArrayList<String> = arrayListOf()
        for (key in balances.keys) {
            dat2.add(key)
        }
        for (i in 0 until dat2.size) {
            result.add(arrayOf(dat2[i], dat[i]!!))
        }
        return result.toArray()
    }

    private fun modelingSeriesForGraph(year: Int): Array<AASeriesElement> {
        val result: ArrayList<AASeriesElement> = arrayListOf()
        val currencies: LinkedList<String> = LinkedList()
        var dataStart: LocalDateTime
        var dataEnd: LocalDateTime = dateTimeFormmatter("${year}-02-01T00:00:00")
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
                dateTimeFormmatter("${year}-0${m}-01T00:00:00")
            else
                dateTimeFormmatter("${year}-${m}-01T00:00:00")
            dataEnd = when {
                m < 9 -> dateTimeFormmatter("${year}-0${m + 1}-01T00:00:00")
                m != 12 -> dateTimeFormmatter("${year}-${m + 1}-01T00:00:00")
                else -> dateTimeFormmatter("${year + 1}-01-01T00:00:00")
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
                if (dateTimeFormmatter(nextTrade!!.dateTime) > date) {
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
                if (dateTimeFormmatter(nextTransaction!!.dateTime) > date) {
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
                if (dateTimeFormmatter(nextTrade!!.dateTime) > dateEnd) {
                    flagTrades = true
                } else {
                    if (dateTimeFormmatter(nextTrade.dateTime) >= dateStart) {
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
                if (dateTimeFormmatter(nextTransaction!!.dateTime) > dateEnd) {
                    flagTransactions = true
                } else {
                    if (dateTimeFormmatter(nextTransaction.dateTime) >= dateStart) {
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

    private fun dateTimeFormmatter(string: String): LocalDateTime {
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
                if (dateTimeFormmatter(nextTrade!!.dateTime) > date) {
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
                if (dateTimeFormmatter(nextTrade!!.dateTime) > dateEnd) {
                    flagTrades = true
                } else {
                    if (dateTimeFormmatter(nextTrade.dateTime) >= dateStart) {
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
