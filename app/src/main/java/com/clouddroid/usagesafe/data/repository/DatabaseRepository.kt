package com.clouddroid.usagesafe.data.repository

import com.clouddroid.usagesafe.data.local.AppLimitDao
import com.clouddroid.usagesafe.data.local.LogEventDao
import com.clouddroid.usagesafe.data.model.AppLimit
import com.clouddroid.usagesafe.data.model.LogEvent
import io.reactivex.Flowable
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DatabaseRepository @Inject constructor(
    private val appLimitsDataSource: AppLimitDao,
    private val logEventsDataSource: LogEventDao
) {

    //this latch is used at every app launch to ensure that the data is first saved
    //to the database before being accessed by history-related fragments
    lateinit var initialSetupLatch: CountDownLatch

    fun addAppLimit(appLimit: AppLimit) {
        appLimitsDataSource.insertAppLimit(appLimit)
    }

    fun deleteAppLimit(appLimit: AppLimit) {
        appLimitsDataSource.deleteAppLimit(appLimit.packageName)
    }

    fun addLogEvents(logEvents: List<LogEvent>) {
        logEventsDataSource.insertLogEvents(logEvents)
    }

    fun getLogEventsFromRange(beginMillis: Long, endMillis: Long): List<LogEvent> {
        return logEventsDataSource.getLogEventsBetweenRange(beginMillis, endMillis)
    }

    fun getTheEarliestLogEvent(): Calendar {
        return Calendar.getInstance()
            .apply {
                timeInMillis =
                    logEventsDataSource.getTheEarliestLogEvent()?.timestamp ?: timeInMillis - TimeUnit.DAYS.toMillis(6)
            }
    }

    fun getListOfLimits(): Flowable<List<AppLimit>> {
        return appLimitsDataSource.getAllLimits()
    }

    fun removeLogsBetweenRange(beginMillis: Long, endMillis: Long) {
        logEventsDataSource.deleteLogEventsBetweenRange(beginMillis, endMillis)
    }
}