package com.clouddroid.usagesafe.data.model

data class AppUsageInfo(
    var packageName: String = "",
    var launchCount: Int = 0,
    var totalTimeInForeground: Long = 0
)