package com.clouddroid.usagesafe.ui.base

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.clouddroid.usagesafe.data.local.DatabaseRepository
import com.clouddroid.usagesafe.util.DayBegin
import com.clouddroid.usagesafe.data.model.LoadingState
import com.clouddroid.usagesafe.data.model.LogEvent
import com.clouddroid.usagesafe.util.WeekBegin
import com.clouddroid.usagesafe.ui.common.WeekViewLogic
import com.clouddroid.usagesafe.ui.common.WeeklyDataMapHolder
import com.clouddroid.usagesafe.util.PreferencesUtils.get
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*

abstract class BaseStatsViewModel(
    protected val databaseRepository: DatabaseRepository,
    sharedPreferences: SharedPreferences
) : ViewModel() {

    val loadingState = MutableLiveData<LoadingState>()
    val shouldLeftArrowBeHidden = MutableLiveData<Boolean>()
    val shouldRightArrowBeHidden = MutableLiveData<Boolean>()

    //stores currently selected week- first and last day respectively
    private val currentWeek = MutableLiveData<Pair<Calendar, Calendar>>()

    //formatted text to be displayed on the bottom view
    val currentWeekText = MutableLiveData<String>()

    //beginning of the week according to user preferences
    private val weekBegin: String = sharedPreferences["week_begin"] ?: WeekBegin.SIX_DAYS_AGO
    private val hourDayBegin: Int = sharedPreferences["day_begin"] ?: DayBegin._12AM

    private val dayOfFirstSavedLog = databaseRepository.getTheEarliestLogEvent()
    private val weekViewLogic =
        WeekViewLogic(Calendar.getInstance(), weekBegin, dayOfFirstSavedLog)

    val weeklyData = MutableLiveData<Map<Long, MutableList<LogEvent>>>()

    fun updateCurrentWeek() {
        loadingState.value = LoadingState.LOADING
        currentWeek.value = Pair(weekViewLogic.currentWeek.first, weekViewLogic.currentWeek.second)
        updateArrowsState()
        setCurrentWeekText(weekViewLogic.currentWeek.first, weekViewLogic.currentWeek.second)
        getLogsFromDB(weekViewLogic.currentWeek.first.timeInMillis, weekViewLogic.currentWeek.second.timeInMillis)
    }

    private fun setCurrentWeekText(firstDayOfWeek: Calendar, lastDayOfWeek: Calendar) {
        val formatter = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
        val firstDayFormatted = formatter.format(Date(firstDayOfWeek.timeInMillis))
        val lastDayFormatted = formatter.format(Date(lastDayOfWeek.timeInMillis))

        currentWeekText.value = "$firstDayFormatted - $lastDayFormatted"
    }

    private fun getLogsFromDB(start: Long, end: Long) {
        val weeklyDataHolder = WeeklyDataMapHolder(start, end, hourDayBegin)

        val disposable = databaseRepository.getLogEventsFromRange(start, end)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { logs ->
                loadingState.value = LoadingState.FINISHED
                weeklyDataHolder.addDayLogs(logs)
                this.weeklyData.value =
                    weeklyDataHolder.logsMap.toSortedMap(Comparator<Long> { o1, o2 -> o1.compareTo(o2) })
            }
    }


    fun rightArrowClicked() {
        weekViewLogic.setNextWeekAsCurrent()
        updateArrowsState()
        updateCurrentWeek()
    }

    fun leftArrowClicked() {
        weekViewLogic.setPreviousWeekAsCurrent()
        updateArrowsState()
        updateCurrentWeek()
    }

    private fun updateArrowsState() {
        shouldLeftArrowBeHidden.value = weekViewLogic.isCurrentWeekTheEarliest
        shouldRightArrowBeHidden.value = weekViewLogic.isCurrentWeekTheLatest
    }

}