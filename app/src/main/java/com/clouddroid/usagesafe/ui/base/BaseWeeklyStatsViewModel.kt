package com.clouddroid.usagesafe.ui.base

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.clouddroid.usagesafe.data.model.LoadingState
import com.clouddroid.usagesafe.data.model.LogEvent
import com.clouddroid.usagesafe.data.repository.DatabaseRepository
import com.clouddroid.usagesafe.ui.common.WeekViewLogic
import com.clouddroid.usagesafe.ui.common.WeeklyDataMapHolder
import com.clouddroid.usagesafe.util.DayBegin
import com.clouddroid.usagesafe.util.PreferencesKeys.PREF_DAY_BEGIN
import com.clouddroid.usagesafe.util.PreferencesKeys.PREF_IS_LAUNCHER_INCLUDED
import com.clouddroid.usagesafe.util.PreferencesKeys.PREF_WEEK_BEGIN
import com.clouddroid.usagesafe.util.PreferencesUtils.get
import com.clouddroid.usagesafe.util.WeekBegin
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*

abstract class BaseWeeklyStatsViewModel(
    protected val databaseRepository: DatabaseRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var disposable: Disposable

    val loadingState = MutableLiveData<LoadingState>()
    val shouldLeftArrowBeHidden = MutableLiveData<Boolean>()
    val shouldRightArrowBeHidden = MutableLiveData<Boolean>()

    //stores currently selected week- first and last day respectively
    private val currentWeek = MutableLiveData<Pair<Calendar, Calendar>>()

    //formatted text to be displayed on the bottom view
    val currentWeekText = MutableLiveData<String>()

    //beginning of the week and day according to user preferences
    private var weekBegin: String = sharedPreferences[PREF_WEEK_BEGIN, WeekBegin.SIX_DAYS_AGO]!!
    private var hourDayBegin: Int = sharedPreferences[PREF_DAY_BEGIN, DayBegin._12AM]!!.toInt()

    val weeklyData = MutableLiveData<Map<Long, MutableList<LogEvent>>>()

    private val dayOfFirstSavedLog = databaseRepository.getTheEarliestLogEvent()
    private val weekViewLogic = WeekViewLogic(Calendar.getInstance(), dayOfFirstSavedLog, weekBegin)

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String) {

        //reacting when user changed beginning of the week
        if (key == PREF_WEEK_BEGIN) {
            weekBegin = prefs[PREF_WEEK_BEGIN, WeekBegin.SIX_DAYS_AGO]!!
            weekViewLogic.weekBegin = weekBegin
            weekViewLogic.refreshWeek()
            updateCurrentWeek()
        }

        //reacting when user changed beginning of the day
        if (key == PREF_DAY_BEGIN) {
            hourDayBegin = prefs[PREF_DAY_BEGIN, DayBegin._12AM]!!.toInt()
            updateCurrentWeek()
        }

        //reacting when user changed launcher preference
        if (key == PREF_IS_LAUNCHER_INCLUDED) {
            updateCurrentWeek()
        }
    }

    fun init() {
        registerPreferencesListener()
        updateCurrentWeek()
    }

    private fun registerPreferencesListener() {
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    fun updateCurrentWeek() {
        //dispose previous calculation if exist
        if (::disposable.isInitialized && !disposable.isDisposed) {
            disposable.dispose()
        }

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

        disposable = Single.fromCallable {
            databaseRepository.initialSetupLatch.await()
            databaseRepository.getLogEventsFromRange(start, end)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { logs ->
                loadingState.value = LoadingState.FINISHED
                weeklyDataHolder.addDayLogs(logs)
                this.weeklyData.value =
                    weeklyDataHolder.logsMap.toSortedMap(Comparator { day1, day2 -> day1.compareTo(day2) })
            }
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
        if (::disposable.isInitialized && !disposable.isDisposed) {
            disposable.dispose()
        }
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

}