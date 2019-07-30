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
import com.clouddroid.usagesafe.data.model.AppLimit
import com.clouddroid.usagesafe.data.model.AppUsageInfo
import com.clouddroid.usagesafe.data.model.FocusModeApp
import com.clouddroid.usagesafe.data.model.LogEvent
import com.clouddroid.usagesafe.data.repository.DatabaseRepository
import com.clouddroid.usagesafe.data.repository.UsageStatsRepository
import com.clouddroid.usagesafe.ui.appblocking.BlockingActivity
import com.clouddroid.usagesafe.ui.appblocking.BlockingMode
import com.clouddroid.usagesafe.ui.main.MainActivity
import com.clouddroid.usagesafe.util.NotificationUtils
import com.clouddroid.usagesafe.util.TextUtils
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
    private var focusModeAppsList = listOf<FocusModeApp>()
    private var isFocusModeEnabled = false

    companion object {
        const val FOCUS_MODE_KEY = "focus_mode"
    }

    override fun onCreate() {
        super.onCreate()
        injectDependencies()
        observeAppLimitsList()
        observeFocusModeAppList()
        updateAppUsageMapPeriodically()
        watchForLaunchingAppWithLimitExceeded()
        Log.d(AppUsageMonitorService::class.java.name, "Service creation")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotification()
        receiveInformationAboutFocusMode(intent)
        Log.d(AppUsageMonitorService::class.java.name, "Service onStartCommand")
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

    private fun observeAppLimitsList() {
        compositeDisposable.add(databaseRepository.getListOfLimits()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                appLimitsList = it
            })
    }

    private fun observeFocusModeAppList() {
        compositeDisposable.add(databaseRepository.getListOfFocusModeApps()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                focusModeAppsList = it
            })
    }

    //updating app usage map every 2 minutes
    private fun updateAppUsageMapPeriodically() {
        compositeDisposable.add(Observable.interval(0, 2, TimeUnit.MINUTES).flatMap {
            Observable.fromCallable {
                usageStatsRepository.getLogsFromToday()
            }
        }
            .doOnNext { logs -> appUsageMap = usageStatsRepository.getAppsUsageMapFromLogs(logs); createNotification() }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { logs ->
                Log.d(AppUsageMonitorService::class.java.name, "Updating map usage")
                checkIfAppInForegroundShouldBeBlocked(logs)
            }

        )
    }

    //checking every 2 seconds if app that has a limit set was launched
    private fun watchForLaunchingAppWithLimitExceeded() {
        compositeDisposable.add(Observable.interval(0, 1, TimeUnit.SECONDS)
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

            //filtering logs to get only those related to app launches
            .map { logs -> logs.filter { logEvent -> logEvent.type == UsageEvents.Event.MOVE_TO_FOREGROUND } }

            //updating information about current app in foreground
            .flatMap { logs ->
                foregroundAppPackageName =
                    if (logs.isNotEmpty()) logs.last().packageName else foregroundAppPackageName

                Log.d(
                    AppUsageMonitorService::class.java.name,
                    "Current app in foreground: $foregroundAppPackageName"
                )

                Observable.just(foregroundAppPackageName)
            }

            //checking if the app in the foreground should be blocked because of exceeded limit or focus mode
            .flatMap { packageName ->
                Observable.just(
                    when {
                        appLimitsList.any { appLimit ->
                            appLimit.packageName == packageName
                                    && appLimit.limit <= appUsageMap[packageName]?.totalTimeInForeground ?: 0
                        } -> BlockingMode.APP_LIMIT
                        focusModeAppsList.any { it.packageName == packageName } && isFocusModeEnabled -> BlockingMode.FOCUS_MODE
                        else -> -1
                    }
                )
            }
            .subscribe { mode ->
                if (mode != -1) displayBlockingActivityWith(mode as BlockingMode)
                applicationContext
            })
    }

    private fun checkIfAppInForegroundShouldBeBlocked(logsList: List<LogEvent>) {
        val appLimit = appLimitsList.find { it.packageName == foregroundAppPackageName }
        Log.d(
            AppUsageMonitorService::class.java.name,
            "Package name to block possibly: ${appLimit?.packageName}"
        )
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
                    appUsageMap[foregroundAppPackageName]?.totalTimeInForeground?.plus(amountOfTimeSinceArriving)
                        ?: 0
                displayBlockingActivityWith(BlockingMode.APP_LIMIT)
            }
        }
    }

    private fun displayBlockingActivityWith(mode: BlockingMode) {
        Intent(this, BlockingActivity::class.java).also { intent ->
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(BlockingActivity.BLOCKING_MODE_KEY, mode)
            applicationContext?.startActivity(intent)
        }
    }

    private fun createNotification() {
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val notification: Notification =
            NotificationCompat.Builder(applicationContext, NotificationUtils.CHANNEL_ID)
                .setContentTitle(getString(R.string.service_app_usage_notification_title))
                .setContentText("${getString(R.string.service_app_usage_notification_description)} ${calculateScreenTime()}")
                .setSmallIcon(R.drawable.ic_service_notificaiton)
                .setContentIntent(pendingIntent)
                .build()

        startForeground(NotificationUtils.APP_USAGE_MONITOR_SERVICE_NOTIFICATION_ID, notification)
    }

    private fun calculateScreenTime(): String {
        return TextUtils.getTotalScreenTimeText(
            appUsageMap.toList().sumBy { it.second.totalTimeInForeground.toInt() }.toLong(),
            this
        )
    }


    private fun receiveInformationAboutFocusMode(intent: Intent?) {
        val receivedData = intent?.extras?.get(FOCUS_MODE_KEY)
        receivedData?.let {
            isFocusModeEnabled = it as Boolean
        }
    }
}