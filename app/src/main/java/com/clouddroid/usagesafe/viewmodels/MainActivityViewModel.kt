package com.clouddroid.usagesafe.viewmodels

import androidx.lifecycle.ViewModel
import com.clouddroid.usagesafe.repositories.DatabaseRepository
import com.clouddroid.usagesafe.repositories.UsageStatsRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(
    private val usageStatsRepository: UsageStatsRepository,
    private val databaseRepository: DatabaseRepository
) : ViewModel() {

    private lateinit var disposable: Disposable

    fun init() {
        disposable = usageStatsRepository.getAppsUsageFromLastWeek()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                databaseRepository.addLogEvent(it)
            }
    }

    override fun onCleared() {
        super.onCleared()
        if (::disposable.isInitialized && !disposable.isDisposed) {
            disposable.dispose()
        }
    }
}