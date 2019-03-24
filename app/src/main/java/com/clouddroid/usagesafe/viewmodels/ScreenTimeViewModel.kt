package com.clouddroid.usagesafe.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.clouddroid.usagesafe.models.AppUsageInfo
import com.clouddroid.usagesafe.models.LogEvent
import com.clouddroid.usagesafe.repositories.UsageStatsRepository
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ScreenTimeViewModel @Inject constructor(
    private val usageStatsRepository: UsageStatsRepository
) : ViewModel() {

    val mostUsedApp = MutableLiveData<AppUsageInfo>()
    val totalTimeSum = MutableLiveData<Long>()
    val barChartData = MutableLiveData<BarData>()

    fun calculateWeeklyUsage(logsMap: Map<Long, MutableList<LogEvent>>) {
        val daysNames = mutableListOf<String>()
        val formatter = SimpleDateFormat("EEE", Locale.getDefault())

        val yVals = mutableListOf<BarEntry>()

        var totalTimeSum = 0L
        val weeklyAppUsage = mutableMapOf<String, AppUsageInfo>()

        var index = 0

        logsMap.forEach { (day, data) ->
            daysNames.add(formatter.format(Date(day)))
            val dailyAppUsageMap = usageStatsRepository.getUsageMapFrom(data)

            dailyAppUsageMap.toList().forEach {
                if (weeklyAppUsage[it.first] == null) weeklyAppUsage[it.first] = AppUsageInfo()
                weeklyAppUsage[it.first]!!.packageName = it.first
                weeklyAppUsage[it.first]!!.totalTimeInForeground += it.second.totalTimeInForeground
                weeklyAppUsage[it.first]!!.launchCount += it.second.launchCount
            }

            dailyAppUsageMap.toList().sumBy { it.second.totalTimeInForeground.toInt() }.apply {
                totalTimeSum += this
                yVals.add(BarEntry(index.toFloat(), this.toFloat() / 1000 / 60))
            }

            index++
        }

        barChartData.value = BarData(BarDataSet(yVals, "Screen time"))
        this.mostUsedApp.value = weeklyAppUsage.toList().maxBy { it.second.totalTimeInForeground }!!.second
        this.totalTimeSum.value = totalTimeSum
    }


}