package com.example.balancecalculation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import co.metalab.asyncawait.async
import com.aachartmodel.aainfographics.aainfographicsLib.aachartConfiger.AAChartView
import com.example.chartcorekotlin.AAChartConfiger.*
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {


    var isMenu1Pressed = false
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        var inf: MenuInflater = menuInflater
        inf.inflate(R.menu.popup, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item?.itemId) {
            R.id.menu1 -> {
                isMenu1Pressed = true
                Start()

                true
            }
            R.id.menu2 -> {
                if (!isMenu1Pressed) {
                    Toast.makeText(this, "You have no transactions/trades", Toast.LENGTH_SHORT)
                        .show()
                    true
                } else {
                    drawGraphPie(finalBalances)
                    showYearsButton?.visibility = View.INVISIBLE
                    true
                }
            }
            R.id.menu3 -> {
                Toast.makeText(this, "What's up?", Toast.LENGTH_SHORT).show()
                true
            }
            else -> false
        }

    }

    var rates: MutableMap<String, BigDecimal> = mutableMapOf()
    var allTrades: ArrayList<Trade>? = arrayListOf()
    var allTransactions: ArrayList<Transaction>? = arrayListOf()
    var showYearsButton: Button? = null
    var years: ArrayList<Int> = arrayListOf()
    var yearForSeriesGraph:Int = 2014
    private var aaChartView: AAChartView? = null
    private var aaChartModel: AAChartModel? = null
    var finalBalances: MutableMap<String, BigDecimal?> = mutableMapOf()

    private val urlTrades = "http://3.248.170.197:9999/bcv/trades"
    private val urlTransactions = "http://3.248.170.197:9999/bcv/transactions"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        aaChartView = findViewById(R.id.AAChartView)
        showYearsButton = findViewById<Button>(R.id.PopupButton)

    }

    private fun Start() {
        rates["BTC"] = BigDecimal(7496.58)
        rates["EOS"] = BigDecimal(2.744)
        rates["ETH"] = BigDecimal(150.2)
        rates["BSV"] = BigDecimal(96.59)
        rates["EOS"] = BigDecimal(2.744)
        rates["EUR"] = BigDecimal(1.11)
        rates["TRY"] = BigDecimal(0.17)
        rates["EURS"] = BigDecimal(1.10471)
        rates["BTG"] = BigDecimal(5.97)
        rates["BCH"] = BigDecimal(213.34)
        rates["LTC"] = BigDecimal(45.56)
        rates["GBP"] = BigDecimal(1.32)
        rates["USDT"] = BigDecimal(1.0003)
        rates["RUB"] = BigDecimal(0.016)
        async {
            allTransactions = await {
                urlTransactions.httpGet().responseObject<ArrayList<Transaction>>().third.get()
            }
            allTrades = await { urlTrades.httpGet().responseObject<ArrayList<Trade>>().third.get() }

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
            for (i in (years.min()!!..years.max()!!)){
                if (!years.contains(i))
                    years.add(i)
            }
            years.sort()
            var popupMenu: PopupMenu? = null
            showYearsButton?.setOnClickListener{
                popupMenu = PopupMenu(this@MainActivity, showYearsButton)
                for (year in years){
                    popupMenu!!.menu.add(year.toString())
                }
                popupMenu!!.menuInflater.inflate(R.menu.popup_year,popupMenu!!.menu)
                popupMenu!!.setOnMenuItemClickListener { item ->
                    yearForSeriesGraph = item.title.toString().toInt()
                    drawGraphColumn()
                    true
                }
                popupMenu!!.show()

            }

            drawGraphColumn()
        }
    }



    private fun CurrencyRateMult(balances: ArrayList<MutableMap<Int, MutableMap<String, BigDecimal?>>>): ArrayList<MutableMap<Int, MutableMap<String, BigDecimal?>>> {
        var rate = BigDecimal(0)
        for (balance in balances) {
            for (month in balance.keys) {
                for (currency in balance[month]!!.keys) {
                    rate = if (currency == "USD") {
                        BigDecimal(1)
                    } else {
                        rates.getValue(currency)
                    }
                    (balance[month]!!)[currency] = ((balance[month]!!)[currency])?.multiply(rate)
                }
            }
        }
        return balances
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
        var dats: ArrayList<String> = arrayListOf()
        for (key in balances?.keys!!) {
            dats.add(key)
        }
        var data = modelingSeriesForPie(balances)
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

    private fun modelingSeriesForPie(balances: MutableMap<String, BigDecimal?>): Array<Any> {
        var result: ArrayList<Any> = arrayListOf()
        var dat: ArrayList<BigDecimal?> = arrayListOf()
        for (value in balances.values!!) {
            if (value != null)
                dat.add(value)
            else dat.add(BigDecimal(0.0))
        }
        var dat2: ArrayList<String> = arrayListOf()
        for (key in balances.keys!!) {
            dat2.add(key)
        }
        for (i in 0 until dat2.size) {
            result.add(arrayOf(dat2[i]!!, dat[i]!!))
        }
        return result.toArray()
    }

    private fun modelingSeriesForGraph(year: Int): Array<AASeriesElement> {
        var result: ArrayList<AASeriesElement> = arrayListOf()
        var month = 1
        var currencies: LinkedList<String> = LinkedList()
        var dataStart: LocalDateTime
        var dataEnd: LocalDateTime = dateTimeFormmatter("${year}-02-01T00:00:00")
        var balanceForMonth = balanceForDateOnlyTrades(dataEnd)
        for (key in balanceForMonth.keys)
            currencies.add(key)
        var yearBalance: ArrayList<MutableMap<String, BigDecimal?>> = arrayListOf()
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
                if (yearBalance[m]?.keys!!.contains(key))
                    yearBalance[m]?.set(key, yearBalance[m]!![key]?.plus(balanceForMonth[key]!!))
                else yearBalance[m][key] = balanceForMonth[key]

            }
        }


        for (key in currencies) {
            var dat: ArrayList<BigDecimal?> = arrayListOf()
            for (month in 1..12) {
                if (yearBalance[month]!!.containsKey(key))
                    dat.add(yearBalance[month]?.getValue(key)!!)
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
        var result: MutableMap<String, BigDecimal?> = mutableMapOf()
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
                            if (nextTrade.tradedQuantityCurrency == "USDT" || nextTrade.tradedPriceCurrency == "USDT") {
                                var k = 2
                            }
                            result[nextTrade.tradedQuantityCurrency] =
                                result[nextTrade.tradedQuantityCurrency]?.minus(nextTrade.tradedQuantity)
                            result[nextTrade.tradedPriceCurrency] =
                                result[nextTrade.tradedPriceCurrency]?.plus(nextTrade.tradedPrice * nextTrade.tradedQuantity - nextTrade.commission)
                        }
                        ("Buy") -> {
                            if (nextTrade.tradedQuantityCurrency == "USDT" || nextTrade.tradedPriceCurrency == "USDT") {
                                var k = 2
                            }
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
                        if (nextTransaction.currency == "USDT") {
                            var k = 2
                            k += 2
                        }
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
        var result: MutableMap<String, BigDecimal?> = mutableMapOf()
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
                    if (nextTrade.tradedPriceCurrency == "USDT" || nextTrade.tradedQuantityCurrency == "USDT") {
                        var k = 2
                        k += 2
                    }
                    if (dateTimeFormmatter(nextTrade!!.dateTime) >= dateStart) {
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
                    if (dateTimeFormmatter(nextTransaction!!.dateTime) >= dateStart) {
                        if (!result.keys.contains(nextTransaction.currency))
                            result[nextTransaction.currency] = BigDecimal(0.0)
                        if (nextTransaction.transactionStatus == "Complete") {
                            if (nextTransaction.currency == "USDT"){
                                var k = 2
                                k += 2
                            }
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
        var result: MutableMap<String, BigDecimal?> = mutableMapOf()
        var flagTrades = false
        var nextTrade: Trade? = null
        if (allTrades == null) {
            flagTrades = true
        } else {
            nextTrade = allTrades!![0]
        }

        var i = 1
        while (!flagTrades)  {

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
                            if (nextTrade.tradedQuantityCurrency == "USDT" || nextTrade.tradedPriceCurrency == "USDT") {
                                var k = 2
                            }
                            result[nextTrade.tradedQuantityCurrency] =
                                result[nextTrade.tradedQuantityCurrency]?.minus(nextTrade.tradedQuantity)
                            if (result[nextTrade.tradedQuantityCurrency]!! < BigDecimal(0))
                                result[nextTrade.tradedQuantityCurrency] = BigDecimal(0)
                            result[nextTrade.tradedPriceCurrency] =
                                result[nextTrade.tradedPriceCurrency]?.plus(nextTrade.tradedPrice * nextTrade.tradedQuantity - nextTrade.commission)
                        }
                        ("Buy") -> {
                            if (nextTrade.tradedQuantityCurrency == "USDT" || nextTrade.tradedPriceCurrency == "USDT") {
                                var k = 2
                            }
                            result[nextTrade.tradedQuantityCurrency] =
                                result[nextTrade.tradedQuantityCurrency]?.plus(nextTrade.tradedQuantity - nextTrade.commission)
                            result[nextTrade.tradedPriceCurrency] =
                                result[nextTrade.tradedPriceCurrency]?.minus(nextTrade.tradedPrice * nextTrade.tradedQuantity)
                            if (result[nextTrade.tradedPriceCurrency]!! < BigDecimal(0))
                                result[nextTrade.tradedPriceCurrency] = BigDecimal(0)
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
        var result: MutableMap<String, BigDecimal?> = mutableMapOf()
        var flagTrades = false
        var nextTrade: Trade? = null
        if (allTrades == null) {
            flagTrades = true
        } else {
            nextTrade = allTrades!![0]
        }

        var i = 1
        while (!flagTrades)  {

            if (!flagTrades) {
                if (dateTimeFormmatter(nextTrade!!.dateTime) > dateEnd) {
                    flagTrades = true
                } else {
                    if (nextTrade.tradedPriceCurrency == "USDT" || nextTrade.tradedQuantityCurrency == "USDT") {
                        var k = 2
                        k += 2
                    }
                    if (dateTimeFormmatter(nextTrade!!.dateTime) >= dateStart) {
                        if (!result.keys.contains(nextTrade.tradedQuantityCurrency))
                            result[nextTrade.tradedQuantityCurrency] = BigDecimal(0.0)
                        if (!result.keys.contains(nextTrade.tradedPriceCurrency))
                            result[nextTrade.tradedPriceCurrency] = BigDecimal(0.0)
                        when (nextTrade.tradeType) {
                            ("Sell") -> {
                                result[nextTrade.tradedQuantityCurrency] =
                                    result[nextTrade.tradedQuantityCurrency]?.minus(nextTrade.tradedQuantity)
                                if (result[nextTrade.tradedQuantityCurrency]!! < BigDecimal(0))
                                    result[nextTrade.tradedQuantityCurrency] = BigDecimal(0)
                                result[nextTrade.tradedPriceCurrency] =
                                    result[nextTrade.tradedPriceCurrency]?.plus(nextTrade.tradedPrice * nextTrade.tradedQuantity - nextTrade.commission)
                            }
                            ("Buy") -> {
                                result[nextTrade.tradedQuantityCurrency] =
                                    result[nextTrade.tradedQuantityCurrency]?.plus(nextTrade.tradedQuantity - nextTrade.commission)
                                result[nextTrade.tradedPriceCurrency] =
                                    result[nextTrade.tradedPriceCurrency]?.minus(nextTrade.tradedPrice * nextTrade.tradedQuantity)
                                if (result[nextTrade.tradedPriceCurrency]!! < BigDecimal(0))
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


}
