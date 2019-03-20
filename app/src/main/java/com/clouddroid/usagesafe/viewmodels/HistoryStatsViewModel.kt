package com.clouddroid.usagesafe.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.clouddroid.usagesafe.models.DayBegin
import com.clouddroid.usagesafe.models.LogEvent
import com.clouddroid.usagesafe.models.WeekBegin
import com.clouddroid.usagesafe.repositories.DatabaseRepository
import com.clouddroid.usagesafe.utils.PreferencesUtils.get
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

class HistoryStatsViewModel @Inject constructor(
    private val databaseRepository: DatabaseRepository,
    sharedPrefs: SharedPreferences
) : ViewModel() {

    //stores currently selected week -first and last day respectively
    private val currentWeek = MutableLiveData<Pair<Calendar, Calendar>>()

    //beginning of the week according to user preferences
    private val weekBegin: String = sharedPrefs["week_begin"] ?: WeekBegin.SIX_DAYS_AGO
    private val hourDayBegin: Int = sharedPrefs["day_begin"] ?: DayBegin._12AM

    private val dayOfFirstSavedLog = databaseRepository.getFirstLogEvent()

    //formatted text to be displayed on the bottom view
    val currentWeekText = MutableLiveData<String>()

    val weeklyData = MutableLiveData<Map<Long, MutableList<LogEvent>>>()

    fun setCurrentWeek(lastDayOfWeek: Calendar) {
        val firstDayOfWeek = getFirstDayOfWeek(lastDayOfWeek).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
        }
        lastDayOfWeek.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
        }
        currentWeek.value = Pair(firstDayOfWeek, lastDayOfWeek)
        setCurrentWeekText(firstDayOfWeek, lastDayOfWeek)
        getLogsFromDB(firstDayOfWeek.timeInMillis, lastDayOfWeek.timeInMillis)
    }

    private fun setCurrentWeekText(firstDayOfWeek: Calendar, lastDayOfWeek: Calendar) {
        val formatter = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
        val firstDayFormatted = formatter.format(Date(firstDayOfWeek.timeInMillis))
        val lastDayFormatted = formatter.format(Date(lastDayOfWeek.timeInMillis))

        currentWeekText.value = "$firstDayFormatted - $lastDayFormatted"
    }

    private fun getLogsFromDB(start: Long, end: Long) {
        val logsMap = HashMap<Long, MutableList<LogEvent>>()
        val calendar = Calendar.getInstance()

        //execute map initialization on different thread to not block the UI
        val disposable = Observable.fromCallable {
            databaseRepository.getLogEventsFromRange(start, end).forEach { logEvent ->
                calendar.timeInMillis = logEvent.timestamp

                //check if this log belongs to previous day according to user preferences
                if (calendar.get(Calendar.HOUR_OF_DAY) < hourDayBegin) calendar.add(Calendar.DAY_OF_MONTH, -1)

                calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (logsMap[calendar.timeInMillis] == null) logsMap[calendar.timeInMillis] = mutableListOf()
                logsMap[calendar.timeInMillis]!!.add(logEvent)
            }
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                weeklyData.value = logsMap
            }
    }

    private fun getFirstDayOfWeek(lastDayOfWeek: Calendar): Calendar {
        return when (weekBegin) {

            WeekBegin.MONDAY -> {
                (lastDayOfWeek.clone() as Calendar).apply {
                    while (get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                        add(Calendar.DAY_OF_MONTH, -1)
                    }
                }
            }

            WeekBegin.SUNDAY -> {
                (lastDayOfWeek.clone() as Calendar).apply {
                    while (get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                        add(Calendar.DAY_OF_MONTH, -1)
                    }
                }
            }

            WeekBegin.SIX_DAYS_AGO -> {
                (lastDayOfWeek.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_MONTH, -6)
                }
            }
            else -> {
                (lastDayOfWeek.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_MONTH, -6)
                }
            }
        }
    }

    fun rightArrowClicked() {
        val currentEndOfWeek = currentWeek.value?.second?.clone() as Calendar

        val todayCalendar = Calendar.getInstance()
        val lastDayOfNextWeek = when (weekBegin) {

            WeekBegin.MONDAY -> {
                currentEndOfWeek.apply {

                    //looking for next Monday
                    while (get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY && before(todayCalendar)) {
                        add(Calendar.DAY_OF_MONTH, 1)
                    }

                    //calculating the end of this week
                    while (get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY && before(todayCalendar)) {
                        add(Calendar.DAY_OF_MONTH, 1)
                    }

                }
            }

            WeekBegin.SUNDAY -> {
                currentEndOfWeek.apply {

                    //looking for next Sunday
                    while (get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY && before(todayCalendar)) {
                        add(Calendar.DAY_OF_MONTH, 1)
                    }

                    //calculating the end of this week
                    while (get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && before(todayCalendar)) {
                        add(Calendar.DAY_OF_MONTH, 1)
                    }
                }
            }

            WeekBegin.SIX_DAYS_AGO -> {
                currentEndOfWeek.apply {
                    if (before(todayCalendar)) add(Calendar.DAY_OF_MONTH, 7)
                }
            }

            else -> {
                currentEndOfWeek.apply {
                    if (before(todayCalendar)) add(Calendar.DAY_OF_MONTH, 7)
                }
            }
        }

        if (!lastDayOfNextWeek.isTheSameDay(currentWeek.value?.second ?: Calendar.getInstance()))
            setCurrentWeek(lastDayOfNextWeek)
    }

    fun leftArrowClicked() {

        val currentBeginOfWeek = currentWeek.value?.first?.clone() as Calendar

        val lastDayOfPreviousWeek: Calendar = when (weekBegin) {

            //look for previous Sunday
            WeekBegin.MONDAY -> {
                currentBeginOfWeek.apply {
                    while (get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                        add(Calendar.DAY_OF_MONTH, -1)
                    }
                }
            }

            //look for previous Saturday
            WeekBegin.SUNDAY -> {
                currentBeginOfWeek.apply {
                    while (get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {
                        add(Calendar.DAY_OF_MONTH, -1)
                    }
                }
            }

            WeekBegin.SIX_DAYS_AGO -> {
                currentBeginOfWeek.apply { add(Calendar.DAY_OF_MONTH, -1) }
            }

            else -> {
                currentBeginOfWeek.apply { add(Calendar.DAY_OF_MONTH, -1) }
            }
        }

        if (!lastDayOfPreviousWeek.isBefore(dayOfFirstSavedLog)) {
            setCurrentWeek(lastDayOfPreviousWeek)
        }
    }

    private fun Calendar.isBefore(other: Calendar): Boolean {
        return (this.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
        }.before(other)
    }

    private fun Calendar.isTheSameDay(other: Calendar): Boolean {
        return this.get(Calendar.YEAR) == other.get(Calendar.YEAR)
                && this.get(Calendar.MONTH) == other.get(Calendar.MONTH)
                && this.get(Calendar.DAY_OF_MONTH) == other.get(Calendar.DAY_OF_MONTH)
    }

    fun getTodayCalendar(): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
        }
    }
}
