package com.clouddroid.usagesafe.repositories

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.util.Log
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class UsageStatsRepository @Inject constructor(private val usageStatsManager: UsageStatsManager) {


    fun getAppsUsageFromToday(): Map<String, AppUsageInfo> {

        val appUsageMap = mutableMapOf<String, UsageStatsRepository.AppUsageInfo>()
        val allEventsList = mutableListOf<UsageEvents.Event>()

        val beginCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 1)
        }

        val endCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
        }

        val events = usageStatsManager.queryEvents(beginCalendar.timeInMillis, endCalendar.timeInMillis)

        var unlockCount = 0

        events?.let {
            while (events.hasNextEvent()) {
                val currentEvent = UsageEvents.Event()
                events.getNextEvent(currentEvent)

                if (currentEvent.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND ||
                    currentEvent.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND
                ) {
                    allEventsList.add(currentEvent)
                }
            }
        }

        for (i in 0 until allEventsList.size - 1) {
            val first = allEventsList[i]
            val second = allEventsList[i + 1]

            //calculating number of phone unlocks
            if (first.packageName == second.packageName
                && first.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND
                && second.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND
            ) {
                unlockCount++
            }

            //calculating number of times opened for each app
            if (first.packageName != second.packageName
                && second.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND
            ) {
                if (appUsageMap[second.packageName] == null) {
                    appUsageMap[second.packageName] = AppUsageInfo()
                }
                appUsageMap[second.packageName]!!.launchCount++
            }

            //calculating total time in foreground for each app
            if (first.className == second.className && first.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND
                && second.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND
            ) {
                if (appUsageMap[second.packageName] == null) {
                    appUsageMap[second.packageName] = AppUsageInfo()
                }
                appUsageMap[second.packageName]!!.totalTimeInForeground += (second.timeStamp - first.timeStamp)
            }
        }

        Log.d("UsageStatsUnlock", unlockCount.toString())
        appUsageMap.forEach { (packageName, appUsageInfo) ->
            Log.d(
                "UsageStats",
                "$packageName- opened: ${appUsageInfo.launchCount}, " +
                        "time: ${TimeUnit.MILLISECONDS.toMinutes(appUsageInfo.totalTimeInForeground)}"
            )
        }

        return appUsageMap
    }


    class AppUsageInfo(var launchCount: Int = 0, var totalTimeInForeground: Long = 0)
}


