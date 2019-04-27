package com.clouddroid.usagesafe.ui.common

import com.clouddroid.usagesafe.data.model.LogEvent
import java.util.*

class WeeklyDataMapHolder(
    private val start: Long,
    private val end: Long,
    private val hourDayBegin: Int
) {

    val logsMap = HashMap<Long, MutableList<LogEvent>>()

    fun addDayLogs(list: List<LogEvent>) {
        logsMap.clear()
        val calendar = Calendar.getInstance()
        list.forEach { logEvent ->
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
    }
}