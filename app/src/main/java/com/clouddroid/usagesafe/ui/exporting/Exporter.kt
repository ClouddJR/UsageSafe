package com.clouddroid.usagesafe.ui.exporting

import com.clouddroid.usagesafe.data.model.AppUsageInfo
import java.text.SimpleDateFormat
import java.util.*

class Exporter {

    enum class Layout {
        ONE_TABLE,
        MULTIPLE_TABLES
    }

    fun export(usageMap: Map<Long, Map<String, AppUsageInfo>>, layout: Layout): String {
        return when (layout) {
            Layout.ONE_TABLE -> exportToOneTable(usageMap)
            Layout.MULTIPLE_TABLES -> exportToMultipleTables(usageMap)
        }
    }

    private fun exportToOneTable(usageMap: Map<Long, Map<String, AppUsageInfo>>): String {
        val stringBuilder = StringBuilder()

        //get all app names
        val appNamesSet = mutableSetOf<String>()
        usageMap.forEach { (_, data) ->
            appNamesSet.addAll(data.keys)
        }
        val appNamesList = appNamesSet.toList().sortedBy { appName -> appName }

        //append the first row with app names
        stringBuilder.append(appNamesList.joinToString(separator = ",", prefix = ",", postfix = ",Total\n"))

        //add new row for each day
        usageMap.forEach { (day, data) ->
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            stringBuilder.append(formatter.format(Date(day))).append(",")
            appNamesList.forEach { name ->
                if (data.containsKey(name)) {
                    stringBuilder.append(data.getValue(name).totalTimeInForeground).append(",")
                } else {
                    stringBuilder.append("0,")
                }
            }

            //add total screen time for each day
            stringBuilder.append(data.values.sumBy { appUsageInfo -> appUsageInfo.totalTimeInForeground.toInt() }
                .toString())
            stringBuilder.append("\n")
        }


        return stringBuilder.toString()
    }

    private fun exportToMultipleTables(usageMap: Map<Long, Map<String, AppUsageInfo>>): String {
        val stringBuilder = StringBuilder()

        //get all app names
        val appNamesSet = mutableSetOf<String>()
        usageMap.forEach { (_, data) ->
            appNamesSet.addAll(data.keys)
        }
        val appNamesList = appNamesSet.toList().sortedBy { appName -> appName }


        //adding each app separately
        appNamesList.forEach { appName ->
            stringBuilder.append("$appName,date,screen_time[ms],app_launches\n")

            //going through each day of logs to check whether this app contains logs from that day
            usageMap.forEach { (day, data) ->
                if (data.containsKey(appName)) {
                    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val date = formatter.format(Date(day))
                    val screenTime = data.getValue(appName).totalTimeInForeground
                    val launchCount = data.getValue(appName).launchCount
                    stringBuilder.append(",$date,$screenTime,$launchCount\n")
                }
            }

            stringBuilder.append("\n")
        }

        return stringBuilder.toString()
    }
}