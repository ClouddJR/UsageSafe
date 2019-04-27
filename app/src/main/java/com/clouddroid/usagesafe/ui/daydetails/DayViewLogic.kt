package com.clouddroid.usagesafe.ui.daydetails

import com.clouddroid.usagesafe.util.ExtensionUtils.isBefore
import com.clouddroid.usagesafe.util.ExtensionUtils.isTheSameDay
import java.util.*

class DayViewLogic(
    private val todayCalendar: Calendar,
    private val dayBegin: Int,
    private val dayOfFirstSavedLog: Calendar
) {

    var currentDay: Calendar

    var isCurrentDayTheEarliest = false
    var isCurrentDayTheLatest = false

    init {
        currentDay = todayCalendar
        checkConstraints()
    }

    fun setCurrentDay(timeInMillis: Long) {
        currentDay = Calendar.getInstance().apply {
            this.timeInMillis = timeInMillis
        }
        checkConstraints()
    }

    fun setNextDayAsCurrent() {
        val nextDay = currentDay.clone() as Calendar
        nextDay.add(Calendar.DAY_OF_MONTH, 1)
        if (nextDay.after(todayCalendar)) nextDay.add(Calendar.DAY_OF_MONTH, -1)
        this.currentDay = nextDay
        checkConstraints()
    }

    fun setPreviousDayAsCurrent() {
        val previousDay = currentDay.clone() as Calendar
        previousDay.add(Calendar.DAY_OF_MONTH, -1)
        if (!previousDay.isBefore(dayOfFirstSavedLog)) {
            currentDay = previousDay
        }
        checkConstraints()
    }

    fun getDayRange(): Pair<Long, Long> {
        val startCalendar = currentDay.clone() as Calendar
        startCalendar.apply {
            set(Calendar.HOUR_OF_DAY, dayBegin)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val endCalendar = (startCalendar.clone() as Calendar).apply {
            add(Calendar.HOUR_OF_DAY, 23)
            add(Calendar.MINUTE, 59)
        }

        return Pair(startCalendar.timeInMillis, endCalendar.timeInMillis)
    }

    private fun checkConstraints() {

        //checking if this is the earliest possible day
        val currentDay = currentDay.clone() as Calendar
        isCurrentDayTheEarliest = currentDay.apply { add(Calendar.DAY_OF_MONTH, -1) }.isBefore(dayOfFirstSavedLog)

        //checking if this is the latest possible day
        isCurrentDayTheLatest = this.currentDay.isTheSameDay(todayCalendar)
    }
}