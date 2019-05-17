package com.clouddroid.usagesafe.ui.applimits.dialog

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.clouddroid.usagesafe.data.repository.DatabaseRepository
import com.clouddroid.usagesafe.data.model.AppDetails
import com.clouddroid.usagesafe.data.model.AppLimit
import com.clouddroid.usagesafe.util.PackageInfoUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AppLimitsDialogViewModel @Inject constructor(
    private val databaseRepository: DatabaseRepository,
    private val packageManager: PackageManager
) :
    ViewModel() {

    val appsList = MutableLiveData<List<AppDetails>>()
    val selectedApp = MutableLiveData<String>()

    private var hourPickerNumber = 0
    private var minutePickerNumber = 0

    private lateinit var disposable: Disposable

    fun getListOfAllApps(context: Context) {
        disposable = Observable.fromCallable { PackageInfoUtils.getLaunchableAppList(packageManager) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .flatMapIterable { it }
            .map { item ->
                AppDetails(
                    packageName = item.activityInfo.packageName,
                    isSystemApp = false,
                    name = PackageInfoUtils.getAppName(item.activityInfo.packageName, context).toString(),
                    icon = PackageInfoUtils.getRawAppIcon(item.activityInfo.packageName, context)
                )
            }
            .toList()
            .toObservable()
            .subscribe {
                appsList.value = it
            }
    }

    fun setAppClicked(packageName: String) {
        selectedApp.value = packageName
    }

    fun updateHourPickerValue(hour: Int) {
        hourPickerNumber = hour
    }

    fun updateMinutePickerValue(minute: Int) {
        minutePickerNumber = minute
    }

    fun saveAppLimit() {
        val limitMillis = getLimitFromHoursAndMinutes(hourPickerNumber, minutePickerNumber)

        val appLimit = AppLimit()
        appLimit.packageName = selectedApp.value ?: ""
        appLimit.limit = limitMillis

        databaseRepository.addAppLimit(appLimit)
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