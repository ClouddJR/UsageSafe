package com.clouddroid.usagesafe.utils

import java.util.concurrent.TimeUnit

object TextUtils {

    fun getTotalScreenTimeText(totalScreenTimeMillis: Long): String {
        var minutes = TimeUnit.MILLISECONDS.toMinutes(totalScreenTimeMillis)
        val hours = minutes / 60
        minutes %= 60

        return if (hours > 0) {
            "$hours hr, $minutes min"
        } else {
            "$minutes min"
        }
    }
}