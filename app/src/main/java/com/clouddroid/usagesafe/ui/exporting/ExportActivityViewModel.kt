package com.clouddroid.usagesafe.ui.exporting

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.clouddroid.usagesafe.data.model.AppUsageInfo
import com.clouddroid.usagesafe.data.model.LogEvent
import com.clouddroid.usagesafe.data.repository.DatabaseRepository
import com.clouddroid.usagesafe.data.repository.UsageStatsRepository
import com.clouddroid.usagesafe.ui.common.WeeklyDataMapHolder
import com.clouddroid.usagesafe.util.DayBegin
import com.clouddroid.usagesafe.util.PackageInfoUtils
import com.clouddroid.usagesafe.util.PreferencesKeys
import com.clouddroid.usagesafe.util.PreferencesUtils.get
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.util.*
import javax.inject.Inject

class ExportActivityViewModel @Inject constructor(
    private val usageStatsRepository: UsageStatsRepository,
    private val databaseRepository: DatabaseRepository,
    private val packageManager: PackageManager,
    sharedPreferences: SharedPreferences
) : ViewModel() {

    private var hourDayBegin: Int = sharedPreferences[PreferencesKeys.PREF_DAY_BEGIN, DayBegin._12AM]!!.toInt()

    val dataSuccessfullySaved = MutableLiveData<String>()
    val pathNotAvailable = MutableLiveData<Boolean>()

    private val compositeDisposable = CompositeDisposable()
    private val exporter = Exporter()

    fun exportData(layout: Exporter.Layout, context: Context) {
        compositeDisposable.add(
            getAllLogsMap()
                .subscribeOn(Schedulers.io())
                .map { logs -> calculateUsage(logs) }
                .map { usageMap -> replacePackageNameWithName(usageMap) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { usageMap ->
                    val data = exporter.export(usageMap, layout)
                    saveDataToAFile(data, context.getExternalFilesDir(null))
                })
    }

    private fun getAllLogsMap(): Single<Map<Long, List<LogEvent>>> {
        return Single.create { emitter ->
            val logs = databaseRepository.getAllLogEvents()
            val weeklyDataHolder = WeeklyDataMapHolder(logs.first().timestamp, logs.last().timestamp, hourDayBegin)
            weeklyDataHolder.addDayLogs(logs)
            emitter.onSuccess(weeklyDataHolder.logsMap)
        }
    }

    private fun calculateUsage(logs: Map<Long, List<LogEvent>>): Map<Long, Map<String, AppUsageInfo>> {
        val usageMap = mutableMapOf<Long, Map<String, AppUsageInfo>>()
        logs.forEach { (day, data) ->
            val dailyAppUsageMap = usageStatsRepository.getAppsUsageMapFromLogs(data)
            usageMap[day] = dailyAppUsageMap
        }

        return usageMap
    }

    private fun replacePackageNameWithName(usageMap: Map<Long, Map<String, AppUsageInfo>>)
            : Map<Long, Map<String, AppUsageInfo>> {
        val newMap = mutableMapOf<Long, Map<String, AppUsageInfo>>()

        usageMap.forEach { (day, data) ->
            val newData = mutableMapOf<String, AppUsageInfo>()
            data.forEach { (packageName, usageInfo) ->
                newData[PackageInfoUtils.getAppName(packageName, packageManager).toString()] = usageInfo
            }

            newMap[day] = newData
        }

        return newMap.toSortedMap(Comparator { day1, day2 -> day1.compareTo(day2) })
    }

    private fun saveDataToAFile(data: String, path: File?) {
        if (path == null) {
            pathNotAvailable.value = true
            return
        }

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        val file = File(path, "data_${year}_${month}_${day}_${hour}_${minute}_$second.csv")
        file.writeText(data)

        dataSuccessfullySaved.value = path.absolutePath
    }
}
