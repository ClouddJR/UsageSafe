package com.clouddroid.usagesafe.viewmodels

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.clouddroid.usagesafe.models.AppDetails
import com.clouddroid.usagesafe.repositories.UsageStatsRepository
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class AppLimitsViewModel @Inject constructor(private val usageStatsRepository: UsageStatsRepository) : ViewModel() {

    val appsList = MutableLiveData<List<AppDetails>>()

    private lateinit var disposable: Disposable

    fun init(context: Context) {
        disposable = Observable.just(usageStatsRepository.getListOfAllApps(context))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                appsList.value = it
            }
    }

    override fun onCleared() {
        super.onCleared()
        if (::disposable.isInitialized && !disposable.isDisposed) {
            disposable.dispose()
        }
    }
}