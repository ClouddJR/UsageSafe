package com.clouddroid.usagesafe.models

data class HourUsageInfo(
    var launchCount: Int = 0,
    var totalTimeInForeground: Long = 0,
    var unlockCount: Int = 0
)