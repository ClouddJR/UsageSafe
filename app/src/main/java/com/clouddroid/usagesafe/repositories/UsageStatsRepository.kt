package com.clouddroid.usagesafe.repositories

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.clouddroid.usagesafe.models.AppDetails
import com.clouddroid.usagesafe.models.AppUsageInfo
import com.clouddroid.usagesafe.models.LogEvent
import com.clouddroid.usagesafe.utils.PackageInfoUtils
import com.clouddroid.usagesafe.utils.PackageInfoUtils.getAppName
import com.clouddroid.usagesafe.utils.PackageInfoUtils.getRawAppIcon
import com.clouddroid.usagesafe.utils.PreferencesUtils.get
import io.reactivex.Observable
import java.util.*
import javax.inject.Inject

class UsageStatsRepository @Inject constructor(
    private val usageStatsManager: UsageStatsManager,
    private val packageManager: PackageManager,
    private val sharedPreferences: SharedPreferences
) {

    fun getAppsUsageFromLastWeek(): Observable<List<LogEvent>> {

        return Observable.create<List<LogEvent>> { emitter ->

            val logs = mutableListOf<LogEvent>()

            val beginCalendar = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_MONTH, -6)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 1)
            }

            val endCalendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
            }

            val events = usageStatsManager.queryEvents(beginCalendar.timeInMillis, endCalendar.timeInMillis)

            while (events.hasNextEvent()) {
                val currentEvent = UsageEvents.Event()
                events.getNextEvent(currentEvent)
                val logEvent = LogEvent()
                logEvent.className = currentEvent.className
                logEvent.timestamp = currentEvent.timeStamp
                logEvent.packageName = currentEvent.packageName
                logEvent.eventType = currentEvent.eventType
                logs.add(logEvent)
            }

            emitter.onNext(logs)
        }
    }

    fun getAppsUsageFromToday(): Pair<Map<String, AppUsageInfo>, Int> {

        var unlockCount = 0
        val allEventsList = mutableListOf<LogEvent>()
        val appUsageMap = mutableMapOf<String, AppUsageInfo>()

        val beginCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 1)
        }

        val endCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
        }

        //getting only relevant events out of all logs (getting only MOVE_TO_BACKGROUND or MOVE_TO_FOREGROUND)
        getAllRelevantEventsToList(
            usageStatsManager.queryEvents(beginCalendar.timeInMillis, endCalendar.timeInMillis),
            allEventsList
        )

        //iterating over events to get different stats
        for (i in 0 until allEventsList.size - 1) {
            val first = allEventsList[i]
            val second = allEventsList[i + 1]

            unlockCount += getPossiblePhoneUnlock(first, second)
            increaseLaunchCount(first, second, appUsageMap)
            increaseTotalTimeInForeground(first, second, appUsageMap)
        }

        //removing launcher if it should not be included
        val isLauncherIncluded = sharedPreferences["is_launcher_included"] ?: false
        if (!isLauncherIncluded) {
            val launcherPackageName = PackageInfoUtils.getDefaultLauncherPackageName(packageManager)
            appUsageMap.remove(launcherPackageName)
        }

        return Pair(appUsageMap, unlockCount)
    }

    fun getUsageMapFrom(logs: List<LogEvent>): MutableMap<String, AppUsageInfo> {
        val relevantLogs = getAllRelevantEventsToList(logs)
        val appUsageMap = mutableMapOf<String, AppUsageInfo>()

        for (i in 0 until relevantLogs.size - 1) {
            val first = relevantLogs[i]
            val second = relevantLogs[i + 1]

            increaseLaunchCount(first, second, appUsageMap)
            increaseTotalTimeInForeground(first, second, appUsageMap)
        }

        //removing launcher if it should not be included
        val isLauncherIncluded = sharedPreferences["is_launcher_included"] ?: false
        if (!isLauncherIncluded) {
            val launcherPackageName = PackageInfoUtils.getDefaultLauncherPackageName(packageManager)
            appUsageMap.remove(launcherPackageName)
        }

        return appUsageMap
    }

    fun getNumberOfUnlocksFrom(logs: List<LogEvent>): Int {
        val relevantLogs = getAllRelevantEventsToList(logs)
        var numberOfUnlocks = 0

        for (i in 0 until relevantLogs.size - 1) {
            val first = relevantLogs[i]
            val second = relevantLogs[i + 1]

            numberOfUnlocks += getPossiblePhoneUnlock(first, second)
        }
        return numberOfUnlocks
    }

    private fun getAllRelevantEventsToList(events: UsageEvents?, allEventsList: MutableList<LogEvent>) {
        events?.let {
            while (events.hasNextEvent()) {
                val currentEvent = UsageEvents.Event()
                events.getNextEvent(currentEvent)

                if (currentEvent.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND ||
                    currentEvent.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND
                ) {
                    allEventsList.add(LogEvent().apply {
                        timestamp = currentEvent.timeStamp
                        packageName = currentEvent.packageName
                        className = currentEvent.className
                        eventType = currentEvent.eventType
                    })
                }
            }
        }
    }

    private fun getAllRelevantEventsToList(logs: List<LogEvent>): List<LogEvent> {
        val list = mutableListOf<LogEvent>()

        logs.forEach { currentEvent ->
            if (currentEvent.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND ||
                currentEvent.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND
            ) {
                list.add(currentEvent)
            }
        }

        return list
    }

    private fun getPossiblePhoneUnlock(first: LogEvent, second: LogEvent): Int {
        return if (first.packageName == second.packageName
            && first.className == second.className
            && first.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND
            && second.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND
        ) {
            1
        } else {
            0
        }
    }

    private fun increaseLaunchCount(
        first: LogEvent,
        second: LogEvent,
        appUsageMap: MutableMap<String, AppUsageInfo>
    ) {
        if (first.packageName != second.packageName
            && second.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND
        ) {
            if (appUsageMap[second.packageName] == null) {
                appUsageMap[second.packageName] = AppUsageInfo().apply {
                    packageName = second.packageName
                }
            }
            appUsageMap[second.packageName]!!.launchCount++
        }

    }

    private fun increaseTotalTimeInForeground(
        first: LogEvent,
        second: LogEvent,
        appUsageMap: MutableMap<String, AppUsageInfo>
    ) {
        if (first.className == second.className && first.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND
            && second.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND
        ) {
            if (appUsageMap[second.packageName] == null) {
                appUsageMap[second.packageName] = AppUsageInfo().apply {
                    packageName = second.packageName
                }
            }
            appUsageMap[second.packageName]!!.totalTimeInForeground += (second.timestamp - first.timestamp)
        }
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


}


