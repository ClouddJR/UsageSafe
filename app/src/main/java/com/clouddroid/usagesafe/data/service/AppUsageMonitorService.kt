package com.clouddroid.usagesafe.data.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.UsageSafeApp
import com.clouddroid.usagesafe.data.local.DatabaseRepository
import com.clouddroid.usagesafe.data.local.UsageStatsRepository
import com.clouddroid.usagesafe.data.model.AppLimit
import com.clouddroid.usagesafe.ui.main.MainActivity
import com.clouddroid.usagesafe.util.NotificationUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AppUsageMonitorService : Service() {

    @Inject
    lateinit var databaseRepository: DatabaseRepository

    @Inject
    lateinit var usageStatsRepository: UsageStatsRepository

    var appLimitsList = listOf<AppLimit>()

    override fun onCreate() {
        super.onCreate()
        (application as UsageSafeApp).component.inject(this)
        observeUsageLogs()
        Log.d(AppUsageMonitorService::class.java.name, "Service creation")
    }

    private fun createNotification() {
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val notification: Notification = NotificationCompat.Builder(applicationContext, NotificationUtils.CHANNEL_ID)
            .setContentTitle("Usage monitor")
            .setContentText("Monitoring app usage")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(NotificationUtils.APP_USAGE_MONITOR_SERVICE_NOTIFICATION_ID, notification)
    }

    private fun updateAppList() {
        appLimitsList = databaseRepository.getListOfLimits()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotification()
        updateAppList()
        return START_STICKY
    }

    private fun observeUsageLogs() {
        val observable = Observable.interval(0, 3, TimeUnit.SECONDS).flatMap {
            Observable.fromCallable {
                val beginCalendar = Calendar.getInstance().apply {
                    add(Calendar.SECOND, -3)
                }
                val endCalendar = Calendar.getInstance()
                usageStatsRepository.getLogsFromRange(beginCalendar.timeInMillis, endCalendar.timeInMillis)
            }
        }
            .observeOn(AndroidSchedulers.mainThread())
            .map { list ->
                list.filter { logEvent ->
                    logEvent.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND
                            && appLimitsList.any { appLimit -> appLimit.packageName == logEvent.packageName }
                }
            }
            .subscribe {
                Log.d(AppUsageMonitorService::class.java.name, "Interval test")
                Log.d(AppUsageMonitorService::class.java.name, it.toString())
                applicationContext
            }
    }

    override fun onBind(intent: Intent?): IBinder? = null

}