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
import com.clouddroid.usagesafe.data.model.AppUsageInfo
import com.clouddroid.usagesafe.data.model.LogEvent
import com.clouddroid.usagesafe.ui.appblocking.BlockingActivity
import com.clouddroid.usagesafe.ui.main.MainActivity
import com.clouddroid.usagesafe.util.NotificationUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AppUsageMonitorService : Service() {

    @Inject
    lateinit var databaseRepository: DatabaseRepository

    @Inject
    lateinit var usageStatsRepository: UsageStatsRepository

    private val compositeDisposable = CompositeDisposable()

    private var foregroundAppPackageName = ""
    private var appUsageMap = mutableMapOf<String, AppUsageInfo>()
    private var appLimitsList = listOf<AppLimit>()

    override fun onCreate() {
        super.onCreate()
        injectDependencies()
        observeAppLimitsList()
        updateAppUsageMapPeriodically()
        watchForLaunchingAppWithLimitExceeded()
        Log.d(AppUsageMonitorService::class.java.name, "Service creation")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotification()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
        Log.d(AppUsageMonitorService::class.java.name, "Service destroyed")
    }

    private fun injectDependencies() {
        (application as UsageSafeApp).component.inject(this)
    }

    //updating app usage map every 2 minutes
    private fun updateAppUsageMapPeriodically() {
        compositeDisposable.add(Observable.interval(0, 2, TimeUnit.MINUTES).flatMap {
            Observable.fromCallable {
                usageStatsRepository.getLogsFromToday()
            }
        }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { logs -> appUsageMap = usageStatsRepository.getAppsUsageMapFromLogs(logs) }
            .subscribe { logs ->
                Log.d(AppUsageMonitorService::class.java.name, "Updating map usage")
                checkIfAppInForegroundShouldBeBlocked(logs)
            }

        )
    }

    //checking every 2 seconds if app that has a limit set was launched
    private fun watchForLaunchingAppWithLimitExceeded() {
        compositeDisposable.add(Observable.interval(0, 2, TimeUnit.SECONDS)
            .flatMap {
                Observable.fromCallable {
                    val beginCalendar = Calendar.getInstance().apply {
                        add(Calendar.SECOND, -2)
                    }
                    val endCalendar = Calendar.getInstance()

                    usageStatsRepository.getLogsFromRange(beginCalendar.timeInMillis, endCalendar.timeInMillis)
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { logs ->
                //updating information about current app in foreground
                foregroundAppPackageName =
                    if (logs.isNotEmpty()) logs.last().packageName else foregroundAppPackageName

                Log.d(
                    AppUsageMonitorService::class.java.name,
                    "Current app in foreground: $foregroundAppPackageName"
                )
            }
            .map { logs ->
                //filtering logs to get only those related to app launches that have a limit set by user
                //and those that have their limit exceeded
                logs.filter { logEvent ->
                    logEvent.type == UsageEvents.Event.MOVE_TO_FOREGROUND && appLimitsList.any { appLimit ->
                        appLimit.packageName == logEvent.packageName
                                && appLimit.limit <= appUsageMap[logEvent.packageName]?.totalTimeInForeground ?: 0
                    }
                }
            }
            .flatMap { list -> Observable.just(list.isNotEmpty()) }
            .subscribe { appShouldBeBlocked ->
                if (appShouldBeBlocked) {
                    displayBlockingActivity()
                }
                applicationContext
            })
    }

    private fun checkIfAppInForegroundShouldBeBlocked(logsList: List<LogEvent>) {
        val appLimit = appLimitsList.find { it.packageName == foregroundAppPackageName }
        Log.d(AppUsageMonitorService::class.java.name, "Package name to block possibly: ${appLimit?.packageName}")
        appLimit?.let {
            val timeOfForegroundAppArriving = logsList.last().timestamp
            val amountOfTimeSinceArriving = Calendar.getInstance().timeInMillis - timeOfForegroundAppArriving

            val foregroundAppUsageTime = appUsageMap[foregroundAppPackageName]?.totalTimeInForeground ?: 0
            Log.d(
                AppUsageMonitorService::class.java.name,
                "Time usage of app to be blocked possibly: ${foregroundAppUsageTime + amountOfTimeSinceArriving}"
            )

            //if today's usage plus current time spent in foreground is more than a limit, we should block this
            if (logsList.last().packageName == foregroundAppPackageName
                && foregroundAppUsageTime + amountOfTimeSinceArriving >= it.limit
            ) {
                //add time spent in foreground to app usage map
                //otherwise we would have to wait couple of minutes for that map to be updated
                appUsageMap[foregroundAppPackageName]?.totalTimeInForeground =
                    appUsageMap[foregroundAppPackageName]?.totalTimeInForeground?.plus(amountOfTimeSinceArriving) ?: 0
                displayBlockingActivity()
            }
        }
    }

    private fun displayBlockingActivity() {
        Intent(this, BlockingActivity::class.java).also { intent ->
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            applicationContext?.startActivity(intent)
        }
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

    private fun observeAppLimitsList() {
        compositeDisposable.add(databaseRepository.getListOfLimits()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                appLimitsList = it
            })
    }

}