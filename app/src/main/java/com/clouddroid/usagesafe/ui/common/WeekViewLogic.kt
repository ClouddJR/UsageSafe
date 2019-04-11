package com.clouddroid.usagesafe.ui.common

import com.clouddroid.usagesafe.util.WeekBegin
import com.clouddroid.usagesafe.util.ExtensionUtils.isBefore
import com.clouddroid.usagesafe.util.ExtensionUtils.isTheSameDay
import java.util.*

class WeekViewLogic(
    private val todayCalendar: Calendar,
    private val weekBegin: String,
    private val dayOfFirstSavedLog: Calendar
) {

    lateinit var currentWeek: Pair<Calendar, Calendar>

    var isCurrentWeekTheEarliest = false
    var isCurrentWeekTheLatest = false

    init {
        setCurrentWeek(todayCalendar)
        checkConstraints(currentWeek.first, currentWeek.second)
    }

    private fun setCurrentWeek(lastDayOfWeek: Calendar) {
        val firstDayOfWeek = getFirstDayOfWeek(lastDayOfWeek).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
        }
        lastDayOfWeek.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
        }
        currentWeek = Pair(firstDayOfWeek, lastDayOfWeek)
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

    fun setNextWeekAsCurrent() {
        val currentEndOfWeek = currentWeek.second.clone() as Calendar

        val lastDayOfNextWeek = getLastDayOfNextWeek(currentEndOfWeek)
        setCurrentWeek(lastDayOfNextWeek)

        checkConstraints(currentWeek.first, currentWeek.second)
    }

    private fun getLastDayOfNextWeek(currentEndOfWeek: Calendar): Calendar {
        return when (weekBegin) {

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
    }

    fun setPreviousWeekAsCurrent() {
        val currentBeginOfWeek = currentWeek.first.clone() as Calendar

        val lastDayOfPreviousWeek = getPreviousWeek(currentBeginOfWeek)
        setCurrentWeek(lastDayOfPreviousWeek)

        checkConstraints(currentWeek.first, currentWeek.second)
    }

    private fun getPreviousWeek(currentBeginOfWeek: Calendar): Calendar {
        return when (weekBegin) {

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
    }

    private fun checkConstraints(currentBeginOfWeek: Calendar, currentEndOfWeek: Calendar) {

        //checking if this is the earliest possible week
        val lastDayOfPreviousWeek = getPreviousWeek(currentBeginOfWeek.clone() as Calendar)
        isCurrentWeekTheEarliest = lastDayOfPreviousWeek.isBefore(dayOfFirstSavedLog)

        //checking if this is the latest possible week
        val lastDayOfNextWeek = getLastDayOfNextWeek(currentEndOfWeek.clone() as Calendar)
        isCurrentWeekTheLatest = lastDayOfNextWeek.isTheSameDay(currentEndOfWeek)
    }
}