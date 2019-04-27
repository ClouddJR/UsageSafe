package com.clouddroid.usagesafe.data.model

data class HourUsageInfo(
    var launchCount: Int = 0,
    var totalTimeInForeground: Long = 0,
    var unlockCount: Int = 0
)