package com.clouddroid.usagesafe.ui.applimits

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.clouddroid.usagesafe.data.model.AppDetails
import com.clouddroid.usagesafe.data.model.AppLimit
import com.clouddroid.usagesafe.data.model.ScreenLimit
import com.clouddroid.usagesafe.data.local.DatabaseRepository
import com.clouddroid.usagesafe.data.local.UsageStatsRepository
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AppLimitsViewModel @Inject constructor(
    private val usageStatsRepository: UsageStatsRepository,
    private val databaseRepository: DatabaseRepository
) : ViewModel() {

    val appsList = MutableLiveData<List<AppDetails>>()

    private lateinit var disposable: Disposable

    fun getListOfApps(context: Context, includeSystemApps: Boolean) {
        disposable = Observable.fromCallable { usageStatsRepository.getListOfAllApps(context, includeSystemApps) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                appsList.value = it
            }
    }

    fun saveAppLimit(packageName: String, hours: Int?, minutes: Int?) {
        val limitMillis = getLimitFromHoursAndMinutes(hours, minutes)

        val appLimit = AppLimit()
        appLimit.packageName = packageName
        appLimit.currentLimit = limitMillis

        databaseRepository.addAppLimit(appLimit)
    }

    fun saveScreenLimit(hours: Int?, minutes: Int?) {
        val limitMillis = getLimitFromHoursAndMinutes(hours, minutes)

        val screenLimit = ScreenLimit()
        screenLimit.limitMillis = limitMillis

        databaseRepository.saveScreenLimit(screenLimit)
    }

    private fun getLimitFromHoursAndMinutes(hours: Int?, minutes: Int?): Long {
        val hoursMillis = TimeUnit.HOURS.toMillis(hours?.toLong() ?: 0)
        val minutesMillis = TimeUnit.MINUTES.toMillis(minutes?.toLong() ?: 0)
        return hoursMillis + minutesMillis
    }

    override fun onCleared() {
        super.onCleared()
        if (::disposable.isInitialized && !disposable.isDisposed) {
            disposable.dispose()
        }
    }


}