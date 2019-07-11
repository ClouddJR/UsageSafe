package com.clouddroid.usagesafe.ui.appdetails

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import com.clouddroid.usagesafe.data.model.LogEvent
import com.clouddroid.usagesafe.data.repository.DatabaseRepository
import com.clouddroid.usagesafe.data.repository.UsageStatsRepository
import com.clouddroid.usagesafe.ui.base.BaseWeeklyStatsViewModel
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class AppDetailsViewModel @Inject constructor(
    private val usageStatsRepository: UsageStatsRepository,
    databaseRepository: DatabaseRepository,
    sharedPrefs: SharedPreferences
) : BaseWeeklyStatsViewModel(databaseRepository, sharedPrefs) {

    object Mode {
        const val SCREEN_TIME = 0
        const val APP_LAUNCHES = 1
    }

    var currentMode = Mode.SCREEN_TIME
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
            val dailyAppUsageMap = usageStatsRepository.getAppsUsageMapFromLogs(data)[packageName]

            totalScreenTime += dailyAppUsageMap?.totalTimeInForeground ?: 0L
            totalLaunchCount += dailyAppUsageMap?.launchCount ?: 0

            screenYVals.add(
                BarEntry(
                    index.toFloat(),
                    dailyAppUsageMap?.totalTimeInForeground?.toFloat() ?: 0f
                )
            )
            launchCountYVals.add(
                BarEntry(
                    index.toFloat(),
                    dailyAppUsageMap?.launchCount?.toFloat() ?: 0f
                )
            )

            index++
        }

        screenTimeBarDataSet = BarDataSet(screenYVals, "Screen time")
        appLaunchesBarDataSet = BarDataSet(launchCountYVals, "App launches")

        when (currentMode) {
            Mode.SCREEN_TIME -> {
                barChartData.value = Pair(screenTimeBarDataSet, daysNames)
                this.totalScreenTimeLiveData.value = totalScreenTime
            }

            Mode.APP_LAUNCHES -> {
                barChartData.value = Pair(appLaunchesBarDataSet, daysNames)
                this.totalLaunchCountLiveData.value = totalLaunchCount
            }
        }

    }

    fun switchMode(mode: Int) {
        when (mode) {
            Mode.SCREEN_TIME -> if (currentMode != Mode.SCREEN_TIME) {
                currentMode = Mode.SCREEN_TIME
                switchToScreenTimeMode()
            }
            Mode.APP_LAUNCHES -> if (currentMode != Mode.APP_LAUNCHES) {
                currentMode = Mode.APP_LAUNCHES
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