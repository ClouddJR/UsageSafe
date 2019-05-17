package com.clouddroid.usagesafe.ui.applimits

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.clouddroid.usagesafe.data.repository.DatabaseRepository
import com.clouddroid.usagesafe.data.model.AppLimit
import com.clouddroid.usagesafe.util.PreferencesKeys.PREF_IS_APP_LIMIT_FEATURE_ENABLED
import com.clouddroid.usagesafe.util.PreferencesUtils.get
import com.clouddroid.usagesafe.util.PreferencesUtils.set
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class AppLimitsViewModel @Inject constructor(
    private val databaseRepository: DatabaseRepository,
    private val sharedPrefs: SharedPreferences
) : ViewModel() {

    val appsList = MutableLiveData<List<AppLimit>>()
    val areAppLimitsEnabled = MutableLiveData<Boolean>()

    private lateinit var disposable: Disposable

    fun init() {
        areAppLimitsEnabled.value = sharedPrefs[PREF_IS_APP_LIMIT_FEATURE_ENABLED]
        getListOfAppLimits()
    }

    private fun getListOfAppLimits() {
        disposable = databaseRepository.getListOfLimits()
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

    fun updateFeatureState(isChecked: Boolean) {
        saveChoiceInPrefs(isChecked)
        areAppLimitsEnabled.value = isChecked
    }

    private fun saveChoiceInPrefs(isChecked: Boolean) {
        sharedPrefs[PREF_IS_APP_LIMIT_FEATURE_ENABLED] = isChecked
    }
}