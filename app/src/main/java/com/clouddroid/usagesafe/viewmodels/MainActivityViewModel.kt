package com.clouddroid.usagesafe.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.clouddroid.usagesafe.models.DayBegin
import com.clouddroid.usagesafe.repositories.DatabaseRepository
import com.clouddroid.usagesafe.repositories.UsageStatsRepository
import com.clouddroid.usagesafe.utils.PreferencesUtils.get
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(
    private val usageStatsRepository: UsageStatsRepository,
    private val databaseRepository: DatabaseRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private lateinit var disposable: Disposable

    fun init() {
        databaseRepository.initialSetupFinished = false
        if (!isLastWeekDataInsideDb()) {
            disposable = Observable.fromCallable {
                usageStatsRepository.getLogsFromLastWeek()
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    databaseRepository.addLogEvent(it) { databaseRepository.initialSetupFinished = true }
                }
        } else {
            removeOldTodayLogs()
            disposable = Observable.fromCallable {
                usageStatsRepository.getLogsFromToday()
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    databaseRepository.addLogEvent(it) { databaseRepository.initialSetupFinished = true }
                }
        }
    }

    private fun removeOldTodayLogs() {
        val hourBegin = sharedPreferences["day_begin"] ?: DayBegin._12AM

        val beginCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourBegin)
        }

        val endCalendar = (beginCalendar.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }

        databaseRepository.removeLogsBetweenRange(beginCalendar.timeInMillis, endCalendar.timeInMillis)
    }

    private fun isLastWeekDataInsideDb(): Boolean {
        val yesterdayBeginCalendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -2)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
        }
        val yesterdayEndCalendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -1)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
        }

        return databaseRepository.getNumberOfLogs(
            yesterdayBeginCalendar.timeInMillis,
            yesterdayEndCalendar.timeInMillis
        ) > 0
    }

    override fun onCleared() {
        super.onCleared()
        if (::disposable.isInitialized && !disposable.isDisposed) {
            disposable.dispose()
        }
    }
}