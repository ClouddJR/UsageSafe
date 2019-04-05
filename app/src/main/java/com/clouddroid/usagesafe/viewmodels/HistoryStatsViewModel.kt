package com.clouddroid.usagesafe.viewmodels

import android.content.SharedPreferences
import com.clouddroid.usagesafe.repositories.DatabaseRepository
import javax.inject.Inject

class HistoryStatsViewModel @Inject constructor(
    databaseRepository: DatabaseRepository,
    sharedPrefs: SharedPreferences
) : BaseStatsViewModel(databaseRepository, sharedPrefs)
