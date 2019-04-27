package com.clouddroid.usagesafe.ui.base

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.clouddroid.usagesafe.data.local.DatabaseRepository
import com.clouddroid.usagesafe.data.model.LoadingState
import com.clouddroid.usagesafe.data.model.LogEvent
import com.clouddroid.usagesafe.ui.common.WeekViewLogic
import com.clouddroid.usagesafe.ui.common.WeeklyDataMapHolder
import com.clouddroid.usagesafe.util.DayBegin
import com.clouddroid.usagesafe.util.PreferencesKeys.PREF_DAY_BEGIN
import com.clouddroid.usagesafe.util.PreferencesKeys.PREF_WEEK_BEGIN
import com.clouddroid.usagesafe.util.PreferencesUtils.get
import com.clouddroid.usagesafe.util.WeekBegin
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*

abstract class BaseWeeklyStatsViewModel(
    protected val databaseRepository: DatabaseRepository,
    sharedPreferences: SharedPreferences
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    val loadingState = MutableLiveData<LoadingState>()
    val shouldLeftArrowBeHidden = MutableLiveData<Boolean>()
    val shouldRightArrowBeHidden = MutableLiveData<Boolean>()

    //stores currently selected week- first and last day respectively
    private val currentWeek = MutableLiveData<Pair<Calendar, Calendar>>()

    //formatted text to be displayed on the bottom view
    val currentWeekText = MutableLiveData<String>()

    //beginning of the week and day according to user preferences
    private val weekBegin: String = sharedPreferences[PREF_WEEK_BEGIN] ?: WeekBegin.SIX_DAYS_AGO
    private val hourDayBegin: Int = sharedPreferences[PREF_DAY_BEGIN] ?: DayBegin._12AM

    val weeklyData = MutableLiveData<Map<Long, MutableList<LogEvent>>>()

    private val dayOfFirstSavedLog = databaseRepository.getTheEarliestLogEvent()
    private val weekViewLogic = WeekViewLogic(Calendar.getInstance(), weekBegin, dayOfFirstSavedLog)

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

        compositeDisposable.add(databaseRepository.getLogEventsFromRange(start, end)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { logs ->
                loadingState.value = LoadingState.FINISHED
                weeklyDataHolder.addDayLogs(logs)
                this.weeklyData.value =
                    weeklyDataHolder.logsMap.toSortedMap(Comparator<Long> { o1, o2 -> o1.compareTo(o2) })
            })
    }


    fun rightArrowClicked() {
        weekViewLogic.setNextWeekAsCurrent()
        updateCurrentWeek()
    }

    fun leftArrowClicked() {
        weekViewLogic.setPreviousWeekAsCurrent()
        updateCurrentWeek()
    }

    private fun updateArrowsState() {
        shouldLeftArrowBeHidden.value = weekViewLogic.isCurrentWeekTheEarliest
        shouldRightArrowBeHidden.value = weekViewLogic.isCurrentWeekTheLatest
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

}