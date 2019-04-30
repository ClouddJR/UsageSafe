package com.clouddroid.usagesafe.ui.applimits

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.clouddroid.usagesafe.data.local.DatabaseRepository
import com.clouddroid.usagesafe.data.model.AppLimit
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class AppLimitsViewModel @Inject constructor(
    private val databaseRepository: DatabaseRepository
) : ViewModel() {

    val appsList = MutableLiveData<List<AppLimit>>()

    private lateinit var disposable: Disposable

    fun getListOfAppLimits() {
        appsList.value = databaseRepository.getListOfLimits()
    }

    override fun onCleared() {
        super.onCleared()
        if (::disposable.isInitialized && !disposable.isDisposed) {
            disposable.dispose()
        }
    }
}