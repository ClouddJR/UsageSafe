package com.clouddroid.usagesafe.ui.main

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.clouddroid.usagesafe.data.local.DatabaseRepository
import com.clouddroid.usagesafe.data.local.UsageStatsRepository
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

        removeOldWeeklyLogs()

        disposable = Observable.fromCallable {
            usageStatsRepository.getLogsFromLastWeek()
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                databaseRepository.addLogEvent(it) { databaseRepository.initialSetupFinished = true }
            }
    }

    private fun removeOldWeeklyLogs() {
        val beginCalendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -6)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 1)
        }

        val endCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
        }

        databaseRepository.removeLogsBetweenRange(beginCalendar.timeInMillis, endCalendar.timeInMillis)
    }

    override fun onCleared() {
        super.onCleared()
        if (::disposable.isInitialized && !disposable.isDisposed) {
            disposable.dispose()
        }
    }
}