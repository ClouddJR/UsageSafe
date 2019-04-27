package com.clouddroid.usagesafe.ui.historystats

import android.content.SharedPreferences
import com.clouddroid.usagesafe.data.local.DatabaseRepository
import com.clouddroid.usagesafe.ui.base.BaseWeeklyStatsViewModel
import javax.inject.Inject

class HistoryStatsViewModel @Inject constructor(
    databaseRepository: DatabaseRepository,
    sharedPrefs: SharedPreferences
) : BaseWeeklyStatsViewModel(databaseRepository, sharedPrefs)
