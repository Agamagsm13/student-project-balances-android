package com.example.balancecalculation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
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

class MainActivity : AppCompatActivity()  {


    var isMenu1Pressed = false
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        var inf: MenuInflater = menuInflater
        inf.inflate(R.menu.popup, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item?.itemId){
            R.id.menu1 ->{
                isMenu1Pressed = true
                Start()
                true
            }
            R.id.menu2 ->{
                if (isMenu1Pressed == false) {
                    Toast.makeText(this, "You have no transactions/trades", Toast.LENGTH_SHORT).show()
                    true
                }
                else {
                    drawGraphPie(finalBalances)

                    true
                }
            }
            R.id.menu3 ->{Toast.makeText(this, "What's up?", Toast.LENGTH_SHORT).show()
                true
            }
            else-> false
        }

    }

    var rates: MutableMap<String, BigDecimal> = mutableMapOf()
    var allTrades: ArrayList<Trade>? = arrayListOf()
    var actualTikers: ArrayList<Tiker>? = arrayListOf()
    var allTransactions: ArrayList<Transaction>? = arrayListOf()
    var years:ArrayList<Int> = ArrayList()
    private var yearCalc:Int = 0
    private var aaChartView: AAChartView? = null
    private var aaChartModel: AAChartModel? = null
    var finalBalances:ArrayList<MutableMap<Int, MutableMap<String,BigDecimal?>>> = ArrayList()

    private val urlTrades = "http://3.248.170.197:9999/bcv/trades"
    private val urlTransactions = "http://3.248.170.197:9999/bcv/transactions"
    private val urlTikersActual = "http://3.248.170.197:8888/bcv/quotes/tikers"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        aaChartView = findViewById(R.id.AAChartView)
    }

    private fun Start(){
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
        actualTikers = await { urlTikersActual.httpGet().responseObject<ArrayList<Tiker>>().third.get() }

        var allTradesTransactions:ArrayList<Any> = arrayListOf()
        if (allTrades != null){
            allTrades!!.sortBy { it.dateTime }
        }
        if (allTransactions != null){
            allTransactions!!.sortBy { it.dateTime }
        }


        var groupedTransactions =
            groupTransactionsByYear(allTransactions)
        var groupedTrades =
            groupTradesByYear(allTrades)                                                                          //группируем сделки по году
                                                               //группируем транзакции по году
        var st: String = ""
        years.sort()
        var resultTrades: MutableMap<Int, MutableMap<String, BigDecimal?>> = mutableMapOf()
        var resultTransaction: MutableMap<Int, MutableMap<String, BigDecimal?>> = mutableMapOf()




        yearCalc = years.first()
        var gt = groupTradesByMonth(groupedTrades[yearCalc])
        for (i in 1..12) {
            resultTrades[i] = calcTradesForMonth(gt[i])
        }

        var gtr = groupTransactionsByMonth(groupedTransactions[yearCalc])
        for (i in 1..12) {
            resultTransaction[i] = calcTransForMonth(gtr[i])
        }
        var yearBalance: MutableMap<Int, MutableMap<String, BigDecimal?>> = mutableMapOf()
        var res: MutableMap<String, BigDecimal?>?
        for (i in 1..12) {
            res = resultTransaction[i]?.let {
                resultTrades[i]?.let { it1 ->
                    finalMonthBalanceCalculation(
                        it,
                        it1
                    )
                }
            }
            if (res != null)
                yearBalance[i] = res
            else yearBalance[i] = mutableMapOf()
        }

        var finalRes = firstYearBalanceCalculation(yearBalance)
        finalBalances.add(finalRes)
        yearCalc++
        while (yearCalc <= years.last()){
            var gt = groupTradesByMonth(groupedTrades[yearCalc])
            for (i in 1..12) {
                resultTrades[i] = calcTradesForMonth(gt[i])
            }

            var gtr = groupTransactionsByMonth(groupedTransactions[yearCalc])
            for (i in 1..12) {
                resultTransaction[i] = calcTransForMonth(gtr[i])
            }
            var yearBalance: MutableMap<Int, MutableMap<String, BigDecimal?>> = mutableMapOf()
            var res: MutableMap<String, BigDecimal?>?
            for (i in 1..12) {
                res = resultTransaction[i]?.let {
                    resultTrades[i]?.let { it1 ->
                        finalMonthBalanceCalculation(
                            it,
                            it1
                        )
                    }
                }
                if (res != null)
                    yearBalance[i] = res
                else yearBalance[i] = mutableMapOf()
            }
            finalRes = finalYearBalanceCalculation((finalBalances.last())[12], yearBalance)
            finalBalances.add(finalRes)
            yearCalc++
        }
        finalBalances = CurrencyRateMult(finalBalances)
        drawGraphColumn(finalBalances)
    }
}


    private fun CurrencyRateMult(balances:ArrayList<MutableMap<Int, MutableMap<String,BigDecimal?>>>):ArrayList<MutableMap<Int, MutableMap<String,BigDecimal?>>>{
        var rate = BigDecimal(0)
        for (balance in balances){
            for (month in balance.keys){
                for (currency in balance[month]!!.keys){
                    rate = if (currency =="USD"){
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










    private fun CurrencyRateMultNotWork(balances:ArrayList<MutableMap<Int, MutableMap<String,BigDecimal?>>>):ArrayList<MutableMap<Int, MutableMap<String,BigDecimal?>>>{
        var rate:BigDecimal? = BigDecimal(0)
        var flag = 0
        var i = 0
        for (balance in balances){
            for (month in balance.keys){
                for (currency in balance[month]!!.keys){
                    flag = 0
                    if (currency.toLowerCase() =="usd"){
                        rate = BigDecimal(1)
                        flag = 1
                    }
                    while(flag == 0){
                        if (i >= actualTikers!!.count()){
                            flag = 1
                            rate = BigDecimal(-1)
                            i = 0
                        }
                        if ( actualTikers?.get(i)?.pair == currency.toLowerCase()+"usd" ){
                            rate = actualTikers?.get(i)?.sell
                            i = 0
                            flag = 1
                        }
                        i++


                    }
                    (balance[month]!!)[currency] = ((balance[month]!!)[currency])?.multiply(rate)
                }
            }
        }
        return balances
    }

    private fun drawGraphColumn(balances:ArrayList<MutableMap<Int, MutableMap<String,BigDecimal?>>>){

        aaChartModel = AAChartModel()
            .title("Portfolio")
            .titleFontColor("#0B1929")
            .titleFontSize(20f)
            .subtitleFontColor("#0B1929")
            .titleFontWeight(AAChartFontWeightType.Bold)
            .subtitle("2019")
            .yAxisTitle("Values in $")
            .chartType(AAChartType.Column)
            .axesTextColor("#0B1929")
            .dataLabelsFontColor("#0B1929")
            .dataLabelsFontWeight(AAChartFontWeightType.Regular)
            .legendEnabled(false)
            .stacking(AAChartStackingType.Normal)
            .colorsTheme(arrayOf("#306FB3","#7291B3","#80A5CC", "#A1CEFF", "#8184CC", "#A1A5FF", "#A56AFF", "#C6A1FF", "#9F81CC", "#DCA1FF", "#322280", "#4732B3", "#8AADFE", "#2B4C99", "#2A9695"))
            .animationType(AAChartAnimationType.Bounce)
            .animationDuration(2000)
            .categories(arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"))
            .series(modelingSeriesForGraph(balances))

        aaChartView?.aa_drawChartWithChartModel(aaChartModel!!)


    }

    private fun drawGraphPie(balances:ArrayList<MutableMap<Int, MutableMap<String,BigDecimal?>>>){
        var dats: ArrayList<String> = arrayListOf()
        for (value in (balances[5])[12]?.keys!!){
            dats.add(value)
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
            .colorsTheme(arrayOf("#306FB3","#7291B3","#80A5CC", "#A1CEFF", "#8184CC", "#A1A5FF", "#A56AFF", "#C6A1FF", "#9F81CC", "#DCA1FF", "#322280", "#4732B3", "#8AADFE", "#2B4C99", "#2A9695"))
            .animationType(AAChartAnimationType.Bounce)
            .categories(dats.toTypedArray())
            .animationDuration(2000)
            .series(arrayOf(
                AASeriesElement()
                    .name("in $")
                    .data(
                        modelingSeriesForPie(finalBalances)

                    )
            )
            )


        aaChartView?.aa_drawChartWithChartModel(aaChartModel!!)


    }

    private fun modelingSeriesForPie(finalBalances:ArrayList<MutableMap<Int, MutableMap<String,BigDecimal?>>>): Array<Any> {
        var result:   ArrayList<Any> = arrayListOf()
        var dat: ArrayList<BigDecimal?> = arrayListOf()
        for (value in (finalBalances[5])[12]?.values!!){
            dat.add(value)
        }
        var dat2: ArrayList<String> = arrayListOf()
        for (value in (finalBalances[5])[12]?.keys!!){
            dat2.add(value)
        }
        for (i in 0 until dat2.size-1){
            result.add(arrayOf(dat2[i]!!, dat[i]!!))
        }
        return result.toArray()
    }

    private fun modelingSeriesForGraph(finalBalances:ArrayList<MutableMap<Int, MutableMap<String,BigDecimal?>>>):Array<AASeriesElement>{
        var result:ArrayList<AASeriesElement> = arrayListOf()
        for (key in (finalBalances[5])[12]!!.keys){
            var dat: ArrayList<BigDecimal?> = arrayListOf()
            for(month in 1..12){
                if((finalBalances[5]!!)[month]!!.containsKey(key))
                    dat.add((finalBalances[5]!!)[month]?.getValue(key)!!)
                else
                    dat.add(BigDecimal(0))
            }
            result.add(AASeriesElement()
                .name(key)
                .data(dat.toArray())
                .stack("1"))
        }
        return result.toTypedArray()
    }

    private fun firstYearBalanceCalculation(balances: MutableMap<Int, MutableMap<String, BigDecimal?>>): MutableMap<Int, MutableMap<String, BigDecimal?>> {
        for (i in 2..12) {
            if (balances[i]?.keys != null)
                for (key in balances[i]!!.keys){
                    if (balances[i - 1]!!.containsKey(key)) {
                        (balances[i])?.set(
                            key,
                            (balances[i])?.get(key)?.add((balances[i - 1])?.get(key))
                        )
                    }
                }
            if (balances[i-1]?.keys != null)
                for (key in balances[i-1]!!.keys){
                    if (!balances[i]?.containsKey(key)!!){
                        balances[i]?.set(key, balances[i-1]?.get(key))
                    }
                }
        }
        return balances
    }




    private fun finalYearBalanceCalculation(lastMonthPrevYear:MutableMap<String, BigDecimal?>?, balances: MutableMap<Int, MutableMap<String, BigDecimal?>>): MutableMap<Int, MutableMap<String, BigDecimal?>> {
        if (yearCalc != years.first())
            for (key in lastMonthPrevYear!!.keys){
                if (balances[1] != null)
                    if (balances[1]!!.containsKey(key)){
                        (balances[1])?.set(key,
                            (balances[1])?.get(key)?.add(lastMonthPrevYear[key])
                        )
                    }
                    else{
                        (balances[1])?.set(key, lastMonthPrevYear[key])
                    }
            }
        for (i in 2..12) {
            if (balances[i]?.keys != null)
                for (key in balances[i]!!.keys)
                    if (balances[i - 1]!!.containsKey(key)) {
                        (balances[i])?.set(
                            key,
                            (balances[i])?.get(key)?.add((balances[i - 1])?.get(key))
                        )
                    }
            if (balances[i-1]?.keys != null)
                for (key in balances[i-1]!!.keys){
                    if (!balances[i]?.containsKey(key)!!){
                        balances[i]?.set(key, balances[i-1]?.get(key))
                    }
                }
        }
        return balances
    }

    private fun finalMonthBalanceCalculation(
        transactions: MutableMap<String, BigDecimal?>,
        trades: MutableMap<String, BigDecimal?>
    ): MutableMap<String, BigDecimal?> {    //считает разницу в каждом месяце
        var result: MutableMap<String, BigDecimal?> = mutableMapOf()
        if (transactions != null) {
            for (transaction in transactions) {
                result[transaction.key] = transaction.value
            }
        }
        if (trades != null) {
            for (trade in trades) {
                if (result.containsKey(trade.key)) {
                    if (trade.value != null)
                        result[trade.key] = result[trade.key]?.plus(trade.value!!)
                } else
                    result[trade.key] = trade.value
            }
        }
        return result

    }


    //-------------All about trades-----------------//
    private fun groupTradesByYear(trades: ArrayList<Trade>?): MutableMap<Int, ArrayList<Trade>> {
        var yearTrades: MutableMap<Int, ArrayList<Trade>> = mutableMapOf()
        var year = 0
        var years:ArrayList<Int> = arrayListOf()
        for (trade in trades!!) {
            year = dateTimeFormmatter(trade.dateTime).year
            if (!years.contains(year))
                years.add(year)
            yearTrades[year] = ArrayList()
        }
        if (trades != null) {
            for (trade in trades) {
                year = dateTimeFormmatter(trade.dateTime).year
                yearTrades.getValue(year).add(trade)

            }
        }
        for (y in years){
            var minId = yearTrades
        }
        for (y in yearTrades.keys)
            if (!years.contains(y))
                years.add(y)
        return yearTrades
    }

    private fun groupTradesByMonth(trades: ArrayList<Trade>?): MutableMap<Int, ArrayList<Trade>> {
        var monthlyTrades: MutableMap<Int, ArrayList<Trade>> = mutableMapOf()
        for (i in 1..12) {
            monthlyTrades[i] = ArrayList()
        }
        var month = 0
        if (trades != null) {
            for (trade in trades) {
                month = dateTimeFormmatter(trade.dateTime).monthValue
                monthlyTrades.getValue(month).add(trade)

            }
        }

        return monthlyTrades
    }

    private fun calcTradesForMonth(trades: ArrayList<Trade>?): MutableMap<String, BigDecimal?> {
        var result: MutableMap<String, BigDecimal?> = mutableMapOf()
       // trades = trades.reversed()
        if (trades != null) {
            for (trade in trades) {
                when (trade.tradeType) {
                    ("Sell") -> {
                        if (result.containsKey(trade.tradedQuantityCurrency)) {
                            result[trade.tradedQuantityCurrency] =
                                result[trade.tradedQuantityCurrency]?.minus(trade.tradedQuantity)
                            if (result.containsKey(trade.tradedPriceCurrency)) {
                                result[trade.tradedPriceCurrency] =
                                    result[trade.tradedPriceCurrency]?.plus(trade.tradedPrice*trade.tradedQuantity)
                            } else
                                result[trade.tradedPriceCurrency] = trade.tradedPrice*trade.tradedQuantity
                        } else {
                            result[trade.tradedQuantityCurrency] = -trade.tradedQuantity
                            if (result.containsKey(trade.tradedPriceCurrency)) {
                                result[trade.tradedPriceCurrency] =
                                    result[trade.tradedPriceCurrency]?.plus(trade.tradedPrice*trade.tradedQuantity)
                            } else
                                result[trade.tradedPriceCurrency] = trade.tradedPrice*trade.tradedQuantity
                        }
                        result[trade.commissionCurrency] =
                            result[trade.commissionCurrency]?.minus(trade.commission)
                    }
                    ("Buy") -> {
                        if (result.containsKey(trade.tradedQuantityCurrency)) {
                            result[trade.tradedQuantityCurrency] =
                                result[trade.tradedQuantityCurrency]?.plus(trade.tradedQuantity)
                            if (result.containsKey(trade.tradedPriceCurrency)) {
                                result[trade.tradedPriceCurrency] =
                                    result[trade.tradedPriceCurrency]?.minus(trade.tradedPrice*trade.tradedQuantity)
                            } else
                                result[trade.tradedPriceCurrency] = -trade.tradedPrice*trade.tradedQuantity
                        } else {
                            result[trade.tradedQuantityCurrency] = trade.tradedQuantity
                            if (result.containsKey(trade.tradedPriceCurrency)) {
                                result[trade.tradedPriceCurrency] =
                                    result[trade.tradedPriceCurrency]?.minus(trade.tradedPrice*trade.tradedQuantity)
                            } else
                                result[trade.tradedPriceCurrency] = -trade.tradedPrice*trade.tradedQuantity
                        }
                        result[trade.commissionCurrency] =
                            result[trade.commissionCurrency]?.minus(trade.commission)
                    }
                }

            }
        }
        return result
    }


    fun getAllTrades() {
        "http://3.248.170.197:9999/bcv/trades"
            .httpGet()
            .responseObject<ArrayList<Trade>> { _, _, result ->
                when (result) {

                    is Result.Success -> {
                        allTrades = result.get()

                    }

                    is Result.Failure -> {
                        println("--------------------------")
                        println(result.getException())
                        println("--------------------------")
                    }
                }
            }
    }

    //------------------About transactions--------------------//


  private fun balanceForDate(date: String):MutableMap<String, BigDecimal?>{
      var result:MutableMap<String, BigDecimal?> = mutableMapOf()
      var nextTrade:Trade? = allTrades?.get(0)
      var nextTransaction:Transaction? = allTransactions?.get(0)
      var flag = false
      if ((nextTrade == null) and (nextTransaction == null))
          flag = true
      else if ((nextTrade != null) and (nextTransaction != null))
          if ((dateTimeFormmatter(nextTrade!!.dateTime) > dateTimeFormmatter(date)) and
              (dateTimeFormmatter(nextTransaction!!.dateTime) > dateTimeFormmatter(date)))
              flag = true
      while (!flag){
          if (nextTrade != null){
              if (!result.keys.contains(nextTrade.tradedQuantityCurrency))
                  result[nextTrade.tradedQuantityCurrency] = BigDecimal(0.0)
              if (!result.keys.contains(nextTrade.tradedPriceCurrency))
                  result[nextTrade.tradedPriceCurrency] = BigDecimal(0.0)
              when (nextTrade.tradeType){
                  ("Sell") -> {
                      result[nextTrade.tradedQuantityCurrency] = result[nextTrade.tradedQuantityCurrency]?.minus(nextTrade.tradedQuantity)
                      result[nextTrade.tradedPriceCurrency] =
                          result[nextTrade.tradedPriceCurrency]?.plus(nextTrade.tradedPrice*nextTrade.tradedQuantity - nextTrade.commission)
                  }
                  ("Buy") -> {
                      result[nextTrade.tradedQuantityCurrency] = result[nextTrade.tradedQuantityCurrency]?.plus(nextTrade.tradedQuantity)
                      result[nextTrade.tradedPriceCurrency] =
                          result[nextTrade.tradedPriceCurrency]?.minus(nextTrade.tradedPrice*nextTrade.tradedQuantity + nextTrade.commission)
                  }
              }
          }
          if (nextTransaction != null){
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

          if ((nextTrade == null) and (nextTransaction == null))
              flag = true
          else if ((nextTrade != null) and (nextTransaction != null))
              if ((dateTimeFormmatter(nextTrade!!.dateTime) > dateTimeFormmatter(date)) and
                  (dateTimeFormmatter(nextTransaction!!.dateTime) > dateTimeFormmatter(date)))
                          flag = true
      }
      return result
  }

    private fun groupTransactionsByYear(transactions: ArrayList<Transaction>?): MutableMap<Int, ArrayList<Transaction>> {
        var yearTransactions: MutableMap<Int, ArrayList<Transaction>> = mutableMapOf()
        var year = 0
        for (transaction in transactions!!) {
            year = dateTimeFormmatter(transaction.dateTime).year
            yearTransactions[year] = ArrayList()
        }
        if (transactions != null) {
            for (transaction in transactions) {
                year = dateTimeFormmatter(transaction.dateTime).year
                if(transaction.transactionStatus == "Complete")
                yearTransactions.getValue(year).add(transaction)

            }
        }
        for (y in yearTransactions.keys)
            years.add(y)
        return yearTransactions
    }

    private fun groupTransactionsByMonth(transactions: ArrayList<Transaction>?): MutableMap<Int, ArrayList<Transaction>> {
        var monthlyTransactions: MutableMap<Int, ArrayList<Transaction>> = mutableMapOf()
        for (i in 1..12) {
            monthlyTransactions[i] = ArrayList()
        }
        var month = 0
        if (transactions != null) {
            for (transaction in transactions) {
                month = dateTimeFormmatter(transaction.dateTime).monthValue
                monthlyTransactions.getValue(month).add(transaction)

            }
        }
        return monthlyTransactions
    }


    private fun calcTransForMonth(transactions: ArrayList<Transaction>?): MutableMap<String, BigDecimal?> {
        var result: MutableMap<String, BigDecimal?> = mutableMapOf()
        if (transactions != null) {
            for (transaction in transactions) {
                if (transaction.transactionStatus == "Complete") {
                    if (result.containsKey(transaction.currency)) {
                        if (transaction.transactionType == "Deposit")
                            result[transaction.currency] =
                                result[transaction.currency]?.plus(transaction.amount - transaction.commission)
                        else {
                            result[transaction.currency] =
                                result[transaction.currency]?.minus(transaction.amount + transaction.commission)
                        }
                    } else {
                        if (transaction.transactionType == "Deposit")
                            result[transaction.currency] = transaction.amount - transaction.commission
                        else
                            result[transaction.currency] = -(transaction.amount + transaction.commission)
                    }
                }
            }
        }
        return result
    }


    private fun dateTimeFormmatter(string: String): LocalDateTime {
        return LocalDateTime.parse(string, DateTimeFormatter.ISO_DATE_TIME)
    }




}
