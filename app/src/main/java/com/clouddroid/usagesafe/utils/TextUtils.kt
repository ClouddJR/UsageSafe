package com.clouddroid.usagesafe.utils

import android.content.Context
import java.util.concurrent.TimeUnit

object TextUtils {

    fun getTotalScreenTimeText(totalScreenTimeMillis: Long, context: Context?): String {
        var minutes = TimeUnit.MILLISECONDS.toMinutes(totalScreenTimeMillis)
        val hours = minutes / 60
        minutes %= 60

        return when {
            hours > 0 -> "$hours hr, $minutes min"
            minutes > 0 -> "$minutes min"
            else -> "${TimeUnit.MILLISECONDS.toSeconds(totalScreenTimeMillis)} sec"
        }
    }
}