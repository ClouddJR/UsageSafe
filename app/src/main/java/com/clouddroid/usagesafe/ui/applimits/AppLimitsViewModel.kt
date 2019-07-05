package com.clouddroid.usagesafe.ui.applimits

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.clouddroid.usagesafe.data.model.AppLimit
import com.clouddroid.usagesafe.data.model.AppUsageInfo
import com.clouddroid.usagesafe.data.repository.DatabaseRepository
import com.clouddroid.usagesafe.data.repository.UsageStatsRepository
import com.clouddroid.usagesafe.util.PreferencesKeys.PREF_IS_FOCUS_MODE_ENABLED
import com.clouddroid.usagesafe.util.PreferencesUtils.get
import com.clouddroid.usagesafe.util.PreferencesUtils.set
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class AppLimitsViewModel @Inject constructor(
    private val databaseRepository: DatabaseRepository,
    private val sharedPrefs: SharedPreferences,
    private val usageStatsRepository: UsageStatsRepository
) : ViewModel() {

    val appsList = MutableLiveData<List<AppLimit>>()
    val usageMap = MutableLiveData<Map<String, AppUsageInfo>>()
    val isFocusModeEnabled = MutableLiveData<Boolean>()

    private val compositeDisposable = CompositeDisposable()

    fun init() {
        isFocusModeEnabled.value = sharedPrefs[PREF_IS_FOCUS_MODE_ENABLED]
        getListOfAppLimits()
        getUsageMapFromToday()
    }

    private fun getListOfAppLimits() {
        compositeDisposable.add(databaseRepository.getListOfLimits()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                appsList.value = it
            })
    }

    private fun getUsageMapFromToday() {
        compositeDisposable.add(Single.fromCallable {
            usageStatsRepository.getUsageFromToday()
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { pair ->
                usageMap.value = pair.first
            })

    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    fun updateFeatureState(isChecked: Boolean) {
        saveChoiceInPrefs(isChecked)
        isFocusModeEnabled.value = isChecked
    }

    private fun saveChoiceInPrefs(isChecked: Boolean) {
        sharedPrefs[PREF_IS_FOCUS_MODE_ENABLED] = isChecked
    }
}