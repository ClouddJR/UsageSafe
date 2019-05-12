package com.clouddroid.usagesafe.ui.daydetails

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.clouddroid.usagesafe.data.local.DatabaseRepository
import com.clouddroid.usagesafe.data.local.UsageStatsRepository
import com.clouddroid.usagesafe.data.model.AppUsageInfo
import com.clouddroid.usagesafe.data.model.LogEvent
import com.clouddroid.usagesafe.util.DayBegin
import com.clouddroid.usagesafe.util.PreferencesKeys.PREF_DAY_BEGIN
import com.clouddroid.usagesafe.util.PreferencesKeys.PREF_TIME_FORMAT
import com.clouddroid.usagesafe.util.PreferencesUtils.get
import com.clouddroid.usagesafe.util.TimeFormat
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class DayDetailsViewModel @Inject constructor(
    private val usageStatsRepository: UsageStatsRepository,
    private val databaseRepository: DatabaseRepository,
    sharedPreferences: SharedPreferences
) : ViewModel() {

    object MODE {
        const val SCREEN_TIME = 0
        const val APP_LAUNCHES = 1
        const val UNLOCKS = 2
    }

    private val compositeDisposable = CompositeDisposable()

    val shouldLeftArrowBeHidden = MutableLiveData<Boolean>()
    val shouldRightArrowBeHidden = MutableLiveData<Boolean>()

    //stores the current mode that a fragment is in
    var currentMode = MODE.SCREEN_TIME

    //formatted text to be displayed on the bottom view
    val currentDayText = MutableLiveData<String>()

    //beginning of the day according to user preferences
    private val hourDayBegin: Int = sharedPreferences[PREF_DAY_BEGIN, DayBegin._12AM]!!.toInt()

    //preferred time format according to user preferences
    private val timeFormat: String = sharedPreferences[PREF_TIME_FORMAT, TimeFormat._12_HOUR_CLOCK]!!

    //handles the logic for changing days
    private val dayOfFirstSavedLog = databaseRepository.getTheEarliestLogEvent()
    private val dayViewLogic =
        DayViewLogic(Calendar.getInstance(), hourDayBegin, dayOfFirstSavedLog)

    //data about daily usage for further calculation and for charts
    private var totalScreenTime = 0L
    private var totalLaunchCount = 0
    private var totalUnlocks = 0
    val totalScreenTimeLiveData = MutableLiveData<Long>()
    val totalLaunchCountLiveData = MutableLiveData<Int>()
    val totalUnlocksLiveData = MutableLiveData<Int>()

    private val appsUsageMap = MutableLiveData<Map<String, AppUsageInfo>>()
    val barChartData = MutableLiveData<Pair<BarDataSet, List<String>>>()
    private val hourNames = mutableListOf<String>()
    private lateinit var screenTimeBarDataSet: BarDataSet
    private lateinit var appLaunchesBarDataSet: BarDataSet
    private lateinit var unlocksBarDataSet: BarDataSet

    fun setCurrentDay(time: Long) {
        dayViewLogic.setCurrentDay(time)
    }

    fun updateCurrentDay() {
        setCurrentDayText(dayViewLogic.currentDay)
        updateArrowsState()
        val range = dayViewLogic.getDayRange()
        getLogsFromDB(range.first, range.second)
    }

    private fun setCurrentDayText(dayCalendar: Calendar) {
        val formatter = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
        val dayFormatted = formatter.format(Date(dayCalendar.timeInMillis))
        currentDayText.value = dayFormatted
    }

    private fun getLogsFromDB(start: Long, end: Long) {
        compositeDisposable.add(databaseRepository.getLogEventsFromRange(start, end)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { logs ->
                getUsageMap(logs)
                calculateDailyUsage(logs, start, end)
            })
    }

    private fun getUsageMap(list: List<LogEvent>) {
        compositeDisposable.add(Single.fromCallable {
            usageStatsRepository.getAppsUsageMapFromLogs(list)
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { usageMap, _ ->
                appsUsageMap.value = usageMap
            })
    }

    fun getAppsUsageList(): LiveData<List<AppUsageInfo>> = Transformations.map(appsUsageMap) {
        it.values.toList()
            .sortedByDescending { it.totalTimeInForeground }
    }

    private fun calculateDailyUsage(logs: List<LogEvent>, start: Long, end: Long) {

        val formatter = when (timeFormat) {
            TimeFormat._12_HOUR_CLOCK -> SimpleDateFormat("h a", Locale.getDefault())
            TimeFormat._24_HOUR_CLOCK -> SimpleDateFormat("H", Locale.getDefault())
            else -> SimpleDateFormat("H", Locale.getDefault())
        }

        val screenYVals = mutableListOf<BarEntry>()
        val launchCountYVals = mutableListOf<BarEntry>()
        val unlockCountYVals = mutableListOf<BarEntry>()

        var index = 0

        val hourlyUsageMap = usageStatsRepository.getHourlyUsageMapFromLogs(logs, start, end)

        totalScreenTime = 0
        totalLaunchCount = 0
        totalUnlocks = 0

        hourlyUsageMap.forEach { (hour, hourData) ->
            hourNames.add(formatter.format(Date(hour)))

            totalScreenTime += hourData.totalTimeInForeground
            totalLaunchCount += hourData.launchCount
            totalUnlocks += hourData.unlockCount

            screenYVals.add(
                BarEntry(
                    index.toFloat(),
                    hourData.totalTimeInForeground.toFloat()
                )
            )

            launchCountYVals.add(
                BarEntry(
                    index.toFloat(),
                    hourData.launchCount.toFloat()
                )
            )
            unlockCountYVals.add(
                BarEntry(
                    index.toFloat(),
                    hourData.unlockCount.toFloat()
                )
            )
            index++
        }

        screenTimeBarDataSet = BarDataSet(screenYVals, "Screen time")
        unlocksBarDataSet = BarDataSet(unlockCountYVals, "Unlocks")
        appLaunchesBarDataSet = BarDataSet(launchCountYVals, "App launches")

        when (currentMode) {
            MODE.SCREEN_TIME -> {
                switchToScreenTimeMode()
            }

            MODE.APP_LAUNCHES -> {
                switchToAppLaunchesMode()
            }

            MODE.UNLOCKS -> {
                switchToUnlockMode()
            }
        }
    }


    fun switchMode(mode: Int) {
        when (mode) {
            MODE.SCREEN_TIME -> if (currentMode != MODE.SCREEN_TIME) {
                currentMode = MODE.SCREEN_TIME
                switchToScreenTimeMode()
            }
            MODE.APP_LAUNCHES -> if (currentMode != MODE.APP_LAUNCHES) {
                currentMode = MODE.APP_LAUNCHES
                switchToAppLaunchesMode()
            }
            MODE.UNLOCKS -> if (currentMode != MODE.UNLOCKS) {
                currentMode = MODE.UNLOCKS
                switchToUnlockMode()
            }
        }
    }

    private fun switchToScreenTimeMode() {
        barChartData.value = Pair(screenTimeBarDataSet, hourNames)
        this.totalScreenTimeLiveData.value = totalScreenTime
    }

    private fun switchToAppLaunchesMode() {
        barChartData.value = Pair(appLaunchesBarDataSet, hourNames)
        this.totalLaunchCountLiveData.value = totalLaunchCount
    }

    private fun switchToUnlockMode() {
        barChartData.value = Pair(unlocksBarDataSet, hourNames)
        this.totalUnlocksLiveData.value = totalUnlocks
    }

    fun rightArrowClicked() {
        dayViewLogic.setNextDayAsCurrent()
        updateArrowsState()
        updateCurrentDay()
    }

    fun leftArrowClicked() {
        dayViewLogic.setPreviousDayAsCurrent()
        updateArrowsState()
        updateCurrentDay()
    }

    private fun updateArrowsState() {
        shouldLeftArrowBeHidden.value = dayViewLogic.isCurrentDayTheEarliest
        shouldRightArrowBeHidden.value = dayViewLogic.isCurrentDayTheLatest
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}