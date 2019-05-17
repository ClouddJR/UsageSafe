package com.clouddroid.usagesafe.ui.todaystats

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.palette.graphics.Palette
import com.clouddroid.usagesafe.data.repository.UsageStatsRepository
import com.clouddroid.usagesafe.data.model.AppUsageInfo
import com.clouddroid.usagesafe.util.PackageInfoUtils.getResizedAppIcon
import com.clouddroid.usagesafe.util.PreferencesKeys
import com.clouddroid.usagesafe.util.TextUtils.getTotalScreenTimeText
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class TodaysStatsViewModel @Inject constructor(
    private val usageStatsRepository: UsageStatsRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val compositeDisposable = CompositeDisposable()

    private val appUsageMap = MutableLiveData<Map<String, AppUsageInfo>>()
    val unlockCount = MutableLiveData<Int>()
    val launchCount = MutableLiveData<Int>()

    val otherAppsList = mutableListOf<AppUsageInfo>()

    fun init() {
        getUsageFromToday()
        registerPreferencesListener()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == PreferencesKeys.PREF_DAY_BEGIN) {
            getUsageFromToday()
        }
    }

    private fun registerPreferencesListener() {
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    private fun getUsageFromToday() {
        compositeDisposable.add(Observable.fromCallable {
            usageStatsRepository.getUsageFromToday()
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val usagePair = it
                appUsageMap.value = usagePair.first
                unlockCount.value = usagePair.second
                launchCount.value = getTotalAppLaunches(it.first)

                otherAppsList.addAll(usagePair.first.toList().sortedByDescending
                { (_, value) -> value.totalTimeInForeground }
                    .drop(5).toMap().values.toMutableList())

            }, {
                it.printStackTrace()
            }))
    }

    fun getAppUsageMap(): LiveData<Map<String, AppUsageInfo>> = Transformations.map(appUsageMap) {
        it.toList()
            .sortedByDescending { (_, value) ->
                value.totalTimeInForeground
            }
            .toMap()
    }

    fun getTotalScreenTime(appUsageMap: Map<String, AppUsageInfo>): Int {
        return appUsageMap.toList().sumBy { it.second.totalTimeInForeground.toInt() }
    }

    fun getTotalScreenTimeText(appUsageMap: Map<String, AppUsageInfo>, context: Context?): String {
        return getTotalScreenTimeText(
            appUsageMap.toList().sumBy { it.second.totalTimeInForeground.toInt() }.toLong()
            , context
        )
    }

    private fun getTotalAppLaunches(appUsageMap: Map<String, AppUsageInfo>): Int {
        return appUsageMap.toList().sumBy { it.second.launchCount }
    }

    fun getMostUsedAppsList(appUsageMap: Map<String, AppUsageInfo>): MutableList<AppUsageInfo> {
        return appUsageMap.toList().take(5).toMap().values.toMutableList()
    }

    fun prepareEntriesForPieChart(appUsageMap: Map<String, AppUsageInfo>, context: Context?): List<PieEntry> {
        val firstAppsUsageMap = getFirstNonZeroAppsUsage(appUsageMap)
        val restAppsUsageMap = getOtherAppsUsage(appUsageMap, firstAppsUsageMap.size)
        return getPieEntriesFromAppsUsage(firstAppsUsageMap, restAppsUsageMap, context)
    }

    private fun getFirstNonZeroAppsUsage(appUsageMap: Map<String, AppUsageInfo>): Map<String, AppUsageInfo> {
        val totalScreenTime = getTotalScreenTime(appUsageMap)

        val firstAppsUsageMap = mutableMapOf<String, AppUsageInfo>()
        appUsageMap.toList().forEachIndexed { index, pair ->
            if (index < 5 && pair.second.totalTimeInForeground > 0
                && (pair.second.totalTimeInForeground / totalScreenTime.toDouble()) > 0.05
            ) {
                firstAppsUsageMap[pair.first] = pair.second
            }
        }
        return firstAppsUsageMap
    }

    private fun getOtherAppsUsage(appUsageMap: Map<String, AppUsageInfo>, beginIndex: Int): Map<String, AppUsageInfo> {
        return appUsageMap.toList().subList(beginIndex, appUsageMap.size).toMap()
    }

    private fun getPieEntriesFromAppsUsage(
        firstAppsUsageMap: Map<String, AppUsageInfo>,
        otherAppsUsageMap: Map<String, AppUsageInfo>,
        context: Context?
    ): List<PieEntry> {

        val entries = mutableListOf<PieEntry>()
        var firstAppsTotalTimeSum = 0
        firstAppsUsageMap.forEach { (packageName, usageInfo) ->
            firstAppsTotalTimeSum += usageInfo.totalTimeInForeground.toInt()
            entries.add(
                PieEntry(
                    usageInfo.totalTimeInForeground.toFloat(),
                    "",
                    getResizedAppIcon(packageName, context),
                    usageInfo.packageName
                )
            )
        }

        val totalOtherTime = otherAppsUsageMap.toList().sumBy { it.second.totalTimeInForeground.toInt() }

        if (totalOtherTime > 0 && (totalOtherTime.div(firstAppsTotalTimeSum.toDouble()) * 100) > 10) {
            entries.add(
                PieEntry(
                    totalOtherTime.toFloat(),
                    "Other",
                    ""
                )
            )
        }

        if (totalOtherTime > 0 && (totalOtherTime.div(firstAppsTotalTimeSum.toDouble()) * 100) <= 10) {
            entries.add(
                PieEntry(
                    totalOtherTime.toFloat(),
                    "",
                    ""
                )
            )
        }

        return entries
    }

    fun generateColorsFromAppIcons(entries: List<PieEntry>): MutableList<Int>? {
        val colors = mutableListOf<Int>()
        entries.forEach { entry ->
            if (entry.icon != null) {
                val palette = createPaletteSync(entry.icon.toBitmap(80, 80))
                colors.add(palette.getDominantColor(ColorTemplate.MATERIAL_COLORS.random()))
            } else {
                colors.add(Color.GRAY)
            }
        }
        return colors
    }

    private fun createPaletteSync(bitmap: Bitmap): Palette = Palette.from(bitmap).generate()

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}