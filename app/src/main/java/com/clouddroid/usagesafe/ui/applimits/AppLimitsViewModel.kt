package com.clouddroid.usagesafe.ui.applimits

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.clouddroid.usagesafe.data.local.DatabaseRepository
import com.clouddroid.usagesafe.data.model.AppLimit
import com.clouddroid.usagesafe.util.PreferencesKeys.PREF_IS_APP_LIMIT_FEATURE_ENABLED
import com.clouddroid.usagesafe.util.PreferencesUtils.get
import com.clouddroid.usagesafe.util.PreferencesUtils.set
import io.reactivex.disposables.Disposable
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
        appsList.value = databaseRepository.getListOfLimits()
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