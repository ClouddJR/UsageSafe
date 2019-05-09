package com.clouddroid.usagesafe.util

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

    fun getAppLimitText(appLimitInMillis: Long): String {
        var minutes = TimeUnit.MILLISECONDS.toMinutes(appLimitInMillis)
        val hours = minutes / 60
        minutes %= 60

        var text = ""

        text += if (hours < 10) "0${hours}h : " else "${hours}h : "
        text += if (minutes < 10) "0${minutes}m" else "${minutes}m"

        return text
    }

}