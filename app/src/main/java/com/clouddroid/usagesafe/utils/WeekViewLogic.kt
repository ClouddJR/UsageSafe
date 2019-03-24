package com.clouddroid.usagesafe.utils

import com.clouddroid.usagesafe.models.WeekBegin
import com.clouddroid.usagesafe.utils.ExtensionUtils.isBefore
import com.clouddroid.usagesafe.utils.ExtensionUtils.isTheSameDay

import java.util.*

class WeekViewLogic(private val weekBegin: String, todayCalendar: Calendar) {

    lateinit var currentWeek: Pair<Calendar, Calendar>

    init {
        setCurrentWeek(todayCalendar)
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

    fun setNextWeekAsActive() {
        val currentEndOfWeek = currentWeek.second.clone() as Calendar

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

        if (!lastDayOfNextWeek.isTheSameDay(currentWeek.second))
            setCurrentWeek(lastDayOfNextWeek)
    }

    fun setPreviousWeekAsActive(dayOfFirstSavedLog: Calendar) {
        val currentBeginOfWeek = currentWeek.first.clone() as Calendar

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
}