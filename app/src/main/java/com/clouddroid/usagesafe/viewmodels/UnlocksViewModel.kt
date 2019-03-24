package com.clouddroid.usagesafe.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.clouddroid.usagesafe.models.LogEvent
import com.clouddroid.usagesafe.repositories.UsageStatsRepository
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class UnlocksViewModel @Inject constructor(
    private val usageStatsRepository: UsageStatsRepository
) : ViewModel() {

    val totalUnlocks = MutableLiveData<Int>()
    val barChartData = MutableLiveData<BarData>()

    fun calculateWeeklyUnlocks(logsMap: Map<Long, MutableList<LogEvent>>) {
        val daysNames = mutableListOf<String>()
        val formatter = SimpleDateFormat("EEE", Locale.getDefault())

        var totalUnlocks = 0
        val yVals = mutableListOf<BarEntry>()

        var index = 0
        logsMap.forEach { (day, data) ->
            daysNames.add(formatter.format(Date(day)))
            val dailyUnlocksNumber = usageStatsRepository.getNumberOfUnlocksFrom(data)

            totalUnlocks += dailyUnlocksNumber
            yVals.add(BarEntry(index.toFloat(), dailyUnlocksNumber.toFloat()))

            index++
        }

        barChartData.value = BarData(BarDataSet(yVals, "Number of unlocks"))
        this.totalUnlocks.value = totalUnlocks
    }

}