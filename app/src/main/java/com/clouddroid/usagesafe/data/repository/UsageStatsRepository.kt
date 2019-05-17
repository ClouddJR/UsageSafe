package com.clouddroid.usagesafe.data.repository

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.clouddroid.usagesafe.data.model.AppDetails
import com.clouddroid.usagesafe.data.model.AppUsageInfo
import com.clouddroid.usagesafe.data.model.HourUsageInfo
import com.clouddroid.usagesafe.data.model.LogEvent
import com.clouddroid.usagesafe.util.DayBegin
import com.clouddroid.usagesafe.util.PackageInfoUtils
import com.clouddroid.usagesafe.util.PackageInfoUtils.getAppName
import com.clouddroid.usagesafe.util.PackageInfoUtils.getRawAppIcon
import com.clouddroid.usagesafe.util.PreferencesKeys.PREF_DAY_BEGIN
import com.clouddroid.usagesafe.util.PreferencesKeys.PREF_IS_LAUNCHER_INCLUDED
import com.clouddroid.usagesafe.util.PreferencesUtils.get
import java.util.*
import javax.inject.Inject

class UsageStatsRepository @Inject constructor(
    private val usageStatsManager: UsageStatsManager,
    private val packageManager: PackageManager,
    private val sharedPreferences: SharedPreferences
) {

    fun getLogsFromLastWeek(): List<LogEvent> {
        val beginCalendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -6)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 1)
        }

        val endCalendar = Calendar.getInstance()

        return getLogsFromRange(beginCalendar.timeInMillis, endCalendar.timeInMillis)
    }

    fun getLogsFromToday(): List<LogEvent> {
        val dayBegin = Integer.parseInt(sharedPreferences[PREF_DAY_BEGIN]!!)

        val beginCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, dayBegin)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endCalendar = (beginCalendar.clone() as Calendar).apply {
            add(Calendar.HOUR_OF_DAY, 23)
            add(Calendar.MINUTE, 59)
        }

        return getLogsFromRange(beginCalendar.timeInMillis, endCalendar.timeInMillis)
    }

    fun getLogsFromRange(beginMillis: Long, endMillis: Long): List<LogEvent> {
        val logs = mutableListOf<LogEvent>()

        val events = usageStatsManager.queryEvents(beginMillis, endMillis)

        while (events.hasNextEvent()) {
            val currentEvent = UsageEvents.Event()
            events.getNextEvent(currentEvent)
            val logEvent = LogEvent()
            logEvent.className = currentEvent.className
            logEvent.timestamp = currentEvent.timeStamp
            logEvent.packageName = currentEvent.packageName
            logEvent.type = currentEvent.eventType
            logs.add(logEvent)
        }

        return logs
    }

    fun getUsageFromToday(): Pair<Map<String, AppUsageInfo>, Int> {

        val dayBegin = sharedPreferences[PREF_DAY_BEGIN, DayBegin._12AM]!!.toInt()

        var unlockCount = 0
        val allEventsList = mutableListOf<LogEvent>()
        val appUsageMap = mutableMapOf<String, AppUsageInfo>()

        val beginCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, dayBegin)
            set(Calendar.MINUTE, 0)
        }

        val endCalendar = (beginCalendar.clone() as Calendar).apply {
            add(Calendar.HOUR_OF_DAY, 23)
            add(Calendar.MINUTE, 59)
        }

        //getting only relevant events out of all logs (getting only MOVE_TO_BACKGROUND or MOVE_TO_FOREGROUND)
        allEventsList.addAll(
            getAllRelevantLogsFromEventsList(
                usageStatsManager.queryEvents(beginCalendar.timeInMillis, endCalendar.timeInMillis)
            )
        )

        //iterating over events to get different stats
        for (i in 0 until allEventsList.size - 1) {
            val first = allEventsList[i]
            val second = allEventsList[i + 1]

            if (didPhoneUnlockOccur(first, second)) unlockCount++
            checkPotentialAppLaunch(first, second, appUsageMap)
            checkPotentialAppExit(first, second, appUsageMap)
        }

        //removing launcher if it should not be included
        val isLauncherIncluded = sharedPreferences[PREF_IS_LAUNCHER_INCLUDED] ?: false
        if (!isLauncherIncluded) {
            val launcherPackageName = PackageInfoUtils.getDefaultLauncherPackageName(packageManager)
            appUsageMap.remove(launcherPackageName)
        }

        return Pair(appUsageMap, unlockCount)
    }

    fun getHourlyUsageMapFromLogs(logs: List<LogEvent>, start: Long, end: Long): MutableMap<Long, HourUsageInfo> {
        val relevantLogs = getAllRelevantLogsFromLogsList(logs)
        val hourUsageMap = mutableMapOf<Long, HourUsageInfo>()

        val isLauncherIncluded = sharedPreferences[PREF_IS_LAUNCHER_INCLUDED] ?: false
        val launcherPackageName = PackageInfoUtils.getDefaultLauncherPackageName(packageManager)

        for (i in 0 until relevantLogs.size - 1) {
            val first = relevantLogs[i]
            val second = relevantLogs[i + 1]

            //getting number of unlocks per hour
            if (didPhoneUnlockOccur(first, second)) {
                val unlockCalendar = Calendar.getInstance().apply {
                    timeInMillis = second.timestamp
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                if (hourUsageMap[unlockCalendar.timeInMillis] == null) hourUsageMap[unlockCalendar.timeInMillis] =
                    HourUsageInfo()
                hourUsageMap[unlockCalendar.timeInMillis]!!.unlockCount++
            }

            //getting number of app launches
            if (first.packageName != second.packageName
                && second.type == UsageEvents.Event.MOVE_TO_FOREGROUND && second.packageName != launcherPackageName
            ) {
                val launchCalendar = Calendar.getInstance().apply {
                    timeInMillis = second.timestamp
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                if (hourUsageMap[launchCalendar.timeInMillis] == null) hourUsageMap[launchCalendar.timeInMillis] =
                    HourUsageInfo()
                hourUsageMap[launchCalendar.timeInMillis]!!.launchCount++
            }

            //skipping launcher events if it should not be included
            //but only after calculating number of app launches and unlocks to not mess stats data
            if (!isLauncherIncluded &&
                (first.packageName == launcherPackageName || second.packageName == launcherPackageName)
            ) {
                continue
            }

            //getting total time in foreground per hour
            val firstEventCalendar = Calendar.getInstance().apply {
                timeInMillis = first.timestamp
            }

            val secondEventCalendar = Calendar.getInstance().apply {
                timeInMillis = second.timestamp
            }

            if (first.className == second.className && first.type == UsageEvents.Event.MOVE_TO_FOREGROUND
                && second.type == UsageEvents.Event.MOVE_TO_BACKGROUND
            ) {
                //app session lasted throughout multiple hours
                while (firstEventCalendar.get(Calendar.HOUR_OF_DAY) != secondEventCalendar.get(Calendar.HOUR_OF_DAY)) {
                    val timestamp = (firstEventCalendar.clone() as Calendar).apply {
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis

                    val nextHourCalendar = (firstEventCalendar.clone() as Calendar).apply {
                        add(Calendar.HOUR_OF_DAY, 1)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    if (hourUsageMap[timestamp] == null) hourUsageMap[timestamp] = HourUsageInfo()
                    hourUsageMap[timestamp]!!.totalTimeInForeground +=
                        (nextHourCalendar.timeInMillis - firstEventCalendar.timeInMillis)

                    firstEventCalendar.timeInMillis = nextHourCalendar.timeInMillis
                }

                val timestamp = (firstEventCalendar.clone() as Calendar).apply {
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                if (hourUsageMap[timestamp] == null) hourUsageMap[timestamp] =
                    HourUsageInfo()
                hourUsageMap[timestamp]!!.totalTimeInForeground +=
                    (secondEventCalendar.timeInMillis - firstEventCalendar.timeInMillis)
            }
        }

        //filling hours not included in db with 0
        val firstHourCalendar = Calendar.getInstance().apply {
            timeInMillis = start
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val lastHourCalendar = Calendar.getInstance().apply {
            timeInMillis = end
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        while (firstHourCalendar.before(lastHourCalendar)) {
            if (hourUsageMap[firstHourCalendar.timeInMillis] == null) {
                hourUsageMap[firstHourCalendar.timeInMillis] = HourUsageInfo()
            }

            firstHourCalendar.add(Calendar.HOUR_OF_DAY, 1)
        }

        if (hourUsageMap[firstHourCalendar.timeInMillis] == null) {
            hourUsageMap[firstHourCalendar.timeInMillis] = HourUsageInfo()
        }

        return hourUsageMap.toSortedMap()
    }

    fun getAppsUsageMapFromLogs(logs: List<LogEvent>): MutableMap<String, AppUsageInfo> {
        val relevantLogs = getAllRelevantLogsFromLogsList(logs)
        val appUsageMap = mutableMapOf<String, AppUsageInfo>()

        for (i in 0 until relevantLogs.size - 1) {
            val first = relevantLogs[i]
            val second = relevantLogs[i + 1]

            checkPotentialAppLaunch(first, second, appUsageMap)
            checkPotentialAppExit(first, second, appUsageMap)
        }

        //removing launcher if it should not be included
        val isLauncherIncluded = sharedPreferences[PREF_IS_LAUNCHER_INCLUDED] ?: false
        if (!isLauncherIncluded) {
            val launcherPackageName = PackageInfoUtils.getDefaultLauncherPackageName(packageManager)
            appUsageMap.remove(launcherPackageName)
        }

        return appUsageMap
    }

    fun getNumberOfUnlocksFromLogs(logs: List<LogEvent>): Int {
        val relevantLogs = getAllRelevantLogsFromLogsList(logs)
        var numberOfUnlocks = 0

        for (i in 0 until relevantLogs.size - 1) {
            val first = relevantLogs[i]
            val second = relevantLogs[i + 1]

            if (didPhoneUnlockOccur(first, second)) numberOfUnlocks++
        }
        return numberOfUnlocks
    }

    fun getListOfAllApps(context: Context, includeSystemApps: Boolean): List<AppDetails> {
        val appsList = mutableListOf<AppDetails>()

        packageManager.getInstalledApplications(PackageManager.GET_META_DATA).forEach { appInfo ->
            if (appInfo.flags and ApplicationInfo.FLAG_SYSTEM == 1) {
                if (includeSystemApps) {
                    appsList.add(
                        AppDetails(
                            packageName = appInfo.packageName,
                            name = getAppName(appInfo.packageName, context).toString(),
                            icon = getRawAppIcon(appInfo.packageName, context),
                            isSystemApp = true
                        )
                    )
                }
            } else if (appInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                appsList.add(
                    AppDetails(
                        packageName = appInfo.packageName,
                        name = getAppName(appInfo.packageName, context).toString(),
                        icon = getRawAppIcon(appInfo.packageName, context),
                        isSystemApp = false
                    )
                )
            }
        }

        return appsList.sortedBy { app -> app.name }
    }

    private fun getAllRelevantLogsFromEventsList(allEvents: UsageEvents?): List<LogEvent> {
        val relevantEventsList = mutableListOf<LogEvent>()
        allEvents?.let {
            while (allEvents.hasNextEvent()) {
                val currentEvent = UsageEvents.Event()
                allEvents.getNextEvent(currentEvent)

                if (currentEvent.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND ||
                    currentEvent.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND
                ) {
                    relevantEventsList.add(LogEvent().apply {
                        timestamp = currentEvent.timeStamp
                        packageName = currentEvent.packageName
                        className = currentEvent.className
                        type = currentEvent.eventType
                    })
                }
            }
        }

        return relevantEventsList
    }

    private fun getAllRelevantLogsFromLogsList(logs: List<LogEvent>): List<LogEvent> {
        val list = mutableListOf<LogEvent>()

        logs.forEach { currentEvent ->
            if (currentEvent.type == UsageEvents.Event.MOVE_TO_BACKGROUND ||
                currentEvent.type == UsageEvents.Event.MOVE_TO_FOREGROUND
            ) {
                list.add(currentEvent)
            }
        }

        return list
    }

    private fun didPhoneUnlockOccur(firstLog: LogEvent, secondLog: LogEvent): Boolean {
        return (firstLog.packageName == secondLog.packageName
                && firstLog.className == secondLog.className
                && firstLog.type == UsageEvents.Event.MOVE_TO_BACKGROUND
                && secondLog.type == UsageEvents.Event.MOVE_TO_FOREGROUND
                && (secondLog.timestamp - firstLog.timestamp >= 200))
    }

    private fun checkPotentialAppLaunch(
        firstLog: LogEvent,
        secondLog: LogEvent,
        appUsageMap: MutableMap<String, AppUsageInfo>
    ) {
        if (firstLog.packageName != secondLog.packageName
            && secondLog.type == UsageEvents.Event.MOVE_TO_FOREGROUND
        ) {
            if (appUsageMap[secondLog.packageName] == null) {
                appUsageMap[secondLog.packageName] = AppUsageInfo().apply {
                    packageName = secondLog.packageName
                }
            }
            appUsageMap[secondLog.packageName]!!.launchCount++
        }

    }

    private fun checkPotentialAppExit(
        firstLog: LogEvent,
        secondLog: LogEvent,
        appUsageMap: MutableMap<String, AppUsageInfo>
    ) {
        if (firstLog.className == secondLog.className && firstLog.type == UsageEvents.Event.MOVE_TO_FOREGROUND
            && secondLog.type == UsageEvents.Event.MOVE_TO_BACKGROUND
        ) {
            if (appUsageMap[secondLog.packageName] == null) {
                appUsageMap[secondLog.packageName] = AppUsageInfo().apply {
                    packageName = secondLog.packageName
                }
            }
            appUsageMap[secondLog.packageName]!!.totalTimeInForeground += (secondLog.timestamp - firstLog.timestamp)
        }
    }


}


