package com.clouddroid.usagesafe.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import com.clouddroid.usagesafe.models.LogEvent
import com.clouddroid.usagesafe.repositories.DatabaseRepository
import com.clouddroid.usagesafe.repositories.UsageStatsRepository
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class AppDetailsViewModel @Inject constructor(
    private val usageStatsRepository: UsageStatsRepository,
    databaseRepository: DatabaseRepository,
    sharedPrefs: SharedPreferences
) : BaseStatsViewModel(databaseRepository, sharedPrefs) {

    object MODE {
        const val MODE_SCREEN_TIME = 0
        const val MODE_APP_LAUNCHES = 1
    }

    var currentMode = MODE.MODE_SCREEN_TIME
    var packageName: String = ""

    private var totalScreenTime = 0L
    private var totalLaunchCount = 0
    val totalScreenTimeLiveData = MutableLiveData<Long>()
    val totalLaunchCountLiveData = MutableLiveData<Int>()

    val barChartData = MutableLiveData<Pair<BarDataSet, List<String>>>()
    private val daysNames = mutableListOf<String>()
    private lateinit var appLaunchesBarDataSet: BarDataSet
    private lateinit var screenTimeBarDataSet: BarDataSet

    fun init(appPackageName: String) {
        packageName = appPackageName
    }

    fun calculateWeeklyUsage(logsMap: Map<Long, MutableList<LogEvent>>) {
        val formatter = SimpleDateFormat("EEE", Locale.getDefault())

        val screenYVals = mutableListOf<BarEntry>()
        val launchCountYVals = mutableListOf<BarEntry>()

        totalScreenTime = 0
        totalLaunchCount = 0
        var index = 0
        logsMap.forEach { (day, data) ->
            daysNames.add(formatter.format(Date(day)))
            val dailyAppUsage = usageStatsRepository.getUsageMapFrom(data)[packageName]

            totalScreenTime += dailyAppUsage?.totalTimeInForeground ?: 0L
            totalLaunchCount += dailyAppUsage?.launchCount ?: 0

            screenYVals.add(
                BarEntry(
                    index.toFloat(),
                    dailyAppUsage?.totalTimeInForeground?.toFloat() ?: 0f
                )
            )
            launchCountYVals.add(
                BarEntry(
                    index.toFloat(),
                    dailyAppUsage?.launchCount?.toFloat() ?: 0f
                )
            )

            index++
        }

        screenTimeBarDataSet = BarDataSet(screenYVals, "Screen time")
        appLaunchesBarDataSet = BarDataSet(launchCountYVals, "App launches")

        when (currentMode) {
            MODE.MODE_SCREEN_TIME -> {
                barChartData.value = Pair(screenTimeBarDataSet, daysNames)
                this.totalScreenTimeLiveData.value = totalScreenTime
            }

            MODE.MODE_APP_LAUNCHES -> {
                barChartData.value = Pair(appLaunchesBarDataSet, daysNames)
                this.totalLaunchCountLiveData.value = totalLaunchCount
            }
        }

    }

    fun switchMode(mode: Int) {
        when (mode) {
            MODE.MODE_SCREEN_TIME -> if (currentMode != MODE.MODE_SCREEN_TIME) {
                currentMode = MODE.MODE_SCREEN_TIME
                switchToScreenTimeMode()
            }
            MODE.MODE_APP_LAUNCHES -> if (currentMode != MODE.MODE_APP_LAUNCHES) {
                currentMode = MODE.MODE_APP_LAUNCHES
                switchToAppLaunchesMode()
            }
        }
    }

    private fun switchToScreenTimeMode() {
        barChartData.value = Pair(screenTimeBarDataSet, daysNames)
        this.totalScreenTimeLiveData.value = totalScreenTime
    }

    private fun switchToAppLaunchesMode() {
        barChartData.value = Pair(appLaunchesBarDataSet, daysNames)
        this.totalLaunchCountLiveData.value = totalLaunchCount
    }
}