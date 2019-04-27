package com.clouddroid.usagesafe.ui.historystats.applaunches

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.clouddroid.usagesafe.data.local.UsageStatsRepository
import com.clouddroid.usagesafe.data.model.AppUsageInfo
import com.clouddroid.usagesafe.data.model.LogEvent
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class AppLaunchesViewModel @Inject constructor(
    private val usageStatsRepository: UsageStatsRepository
) : ViewModel() {

    val mostOpenedApp = MutableLiveData<AppUsageInfo>()
    val totalLaunchCount = MutableLiveData<Int>()
    val barChartData = MutableLiveData<Pair<BarDataSet, List<String>>>()

    fun calculateWeeklyLaunches(logsMap: Map<Long, MutableList<LogEvent>>) {
        val daysNames = mutableListOf<String>()
        val formatter = SimpleDateFormat("EEE", Locale.getDefault())

        val yVals = mutableListOf<BarEntry>()

        var totalLaunchCount = 0
        val weeklyAppUsage = mutableMapOf<String, AppUsageInfo>()

        var index = 0

        logsMap.forEach { (day, data) ->
            daysNames.add(formatter.format(Date(day)))
            val dailyAppUsageMap = usageStatsRepository.getAppsUsageMapFromLogs(data)

            dailyAppUsageMap.toList().forEach {
                if (weeklyAppUsage[it.first] == null) weeklyAppUsage[it.first] =
                    AppUsageInfo()
                weeklyAppUsage[it.first]?.apply {
                    packageName = it.first
                    totalTimeInForeground += it.second.totalTimeInForeground
                    launchCount += it.second.launchCount
                }
            }

            dailyAppUsageMap.toList().sumBy { it.second.launchCount }.apply {
                totalLaunchCount += this
                yVals.add(BarEntry(index.toFloat(), this.toFloat(), day))
            }

            index++
        }

        barChartData.value = Pair(BarDataSet(yVals, ""), daysNames)
        this.totalLaunchCount.value = totalLaunchCount
        this.mostOpenedApp.value = weeklyAppUsage.toList().maxBy { it.second.launchCount }!!.second
    }


}