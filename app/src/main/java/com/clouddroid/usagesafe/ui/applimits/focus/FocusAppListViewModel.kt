package com.clouddroid.usagesafe.ui.applimits.focus

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.clouddroid.usagesafe.data.model.AppDetails
import com.clouddroid.usagesafe.data.model.FocusModeApp
import com.clouddroid.usagesafe.data.repository.DatabaseRepository
import com.clouddroid.usagesafe.util.PackageInfoUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class FocusAppListViewModel @Inject constructor(
    private val databaseRepository: DatabaseRepository,
    private val packageManager: PackageManager
) : ViewModel() {

    val adapterData = MutableLiveData<Pair<List<FocusModeApp>, List<AppDetails>>>()

    private lateinit var disposable: Disposable

    override fun onCleared() {
        super.onCleared()
        if (::disposable.isInitialized && !disposable.isDisposed) {
            disposable.dispose()
        }
    }

    fun getAdapterData(context: Context) {
        disposable =
            Observable.zip(databaseRepository.getListOfFocusModeApps().toObservable(), getLaunchableApps(context),
                BiFunction<List<FocusModeApp>, List<AppDetails>, Pair<List<FocusModeApp>, List<AppDetails>>>()
                { focusModeApps, launchableApps ->
                    Pair(
                        focusModeApps,
                        launchableApps
                    )
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    adapterData.value = it
                }
    }


    private fun getLaunchableApps(context: Context): Observable<List<AppDetails>> {
        return Observable.fromCallable { PackageInfoUtils.getLaunchableAppList(packageManager) }
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
    }

    fun addOrRemoveAppFromFocusAppsList(appDetails: AppDetails) {
        when (appDetails.isInFocusMode) {
            true -> databaseRepository.addFocusModeApp(FocusModeApp(packageName = appDetails.packageName))
            false -> databaseRepository.deleteFocusModeApp(FocusModeApp(packageName = appDetails.packageName))
        }
    }
}