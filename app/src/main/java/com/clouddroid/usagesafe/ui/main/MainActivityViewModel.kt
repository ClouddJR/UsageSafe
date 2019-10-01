package com.clouddroid.usagesafe.ui.main

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.clouddroid.usagesafe.data.model.LogEvent
import com.clouddroid.usagesafe.data.repository.DatabaseRepository
import com.clouddroid.usagesafe.data.repository.UsageStatsRepository
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.CountDownLatch
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(
    private val usageStatsRepository: UsageStatsRepository,
    private val databaseRepository: DatabaseRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private lateinit var disposable: Disposable

    fun init() {
        databaseRepository.initialSetupLatch = CountDownLatch(1)
        disposable = Single.fromCallable {
            usageStatsRepository.getLogsFromLastWeek()
        }
            .flatMapCompletable {
                Completable.fromAction {
                    if (it.isNotEmpty()) {
                        removeOldLogsFromDatabaseStartingWith(it.sortedBy { it.timestamp }.first())
                        databaseRepository.addLogEvents(it)
                    }
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                databaseRepository.initialSetupLatch.countDown()
            }, {
                databaseRepository.initialSetupLatch.countDown()
            })
    }

    private fun removeOldLogsFromDatabaseStartingWith(firstLogEvent: LogEvent) {
        val endCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
        }

        databaseRepository.deleteLogsBetweenRange(firstLogEvent.timestamp, endCalendar.timeInMillis)
    }

    override fun onCleared() {
        super.onCleared()
        if (::disposable.isInitialized && !disposable.isDisposed) {
            disposable.dispose()
        }
    }
}