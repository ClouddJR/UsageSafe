package com.clouddroid.usagesafe.viewmodels

import androidx.lifecycle.ViewModel
import com.clouddroid.usagesafe.repositories.UsageStatsRepository
import javax.inject.Inject

class TodaysStatsViewModel @Inject constructor(private val usageStatsRepository: UsageStatsRepository) : ViewModel() {

    fun getTodaysStats() {
        usageStatsRepository.getAppsUsageFromToday()
    }
}