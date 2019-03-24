package com.clouddroid.usagesafe.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.clouddroid.usagesafe.models.DayBegin
import com.clouddroid.usagesafe.models.LogEvent
import com.clouddroid.usagesafe.models.WeekBegin
import com.clouddroid.usagesafe.repositories.DatabaseRepository
import com.clouddroid.usagesafe.utils.PreferencesUtils.get
import com.clouddroid.usagesafe.utils.WeekViewLogic
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

    //stores currently selected week- first and last day respectively
    private val currentWeek = MutableLiveData<Pair<Calendar, Calendar>>()

    //formatted text to be displayed on the bottom view
    val currentWeekText = MutableLiveData<String>()

    //beginning of the week according to user preferences
    private val weekBegin: String = sharedPrefs["week_begin"] ?: WeekBegin.SIX_DAYS_AGO
    private val hourDayBegin: Int = sharedPrefs["day_begin"] ?: DayBegin._12AM

    private val weekViewLogic = WeekViewLogic(weekBegin, Calendar.getInstance())

    val weeklyData = MutableLiveData<Map<Long, MutableList<LogEvent>>>()

    private val dayOfFirstSavedLog = databaseRepository.getFirstLogEvent()

    fun updateCurrentWeek() {
        currentWeek.value = Pair(weekViewLogic.currentWeek.first, weekViewLogic.currentWeek.second)
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

            //filling days not included in db with zero
            val firstDayCalendar = Calendar.getInstance().apply {
                timeInMillis = start
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val lastDayCalendar = Calendar.getInstance().apply {
                timeInMillis = end
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            while (firstDayCalendar.before(lastDayCalendar)) {
                if (logsMap[firstDayCalendar.timeInMillis] == null) {
                    logsMap[firstDayCalendar.timeInMillis] = mutableListOf()
                }

                firstDayCalendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            if (logsMap[firstDayCalendar.timeInMillis] == null) {
                logsMap[firstDayCalendar.timeInMillis] = mutableListOf()
            }

        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                weeklyData.value = logsMap.toSortedMap(Comparator<Long> { o1, o2 -> o1.compareTo(o2) })
            }
    }

    fun rightArrowClicked() {
        weekViewLogic.setNextWeekAsActive()
        updateCurrentWeek()
    }

    fun leftArrowClicked() {
        weekViewLogic.setPreviousWeekAsActive(dayOfFirstSavedLog)
        updateCurrentWeek()
    }
}
