package com.clouddroid.usagesafe.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.palette.graphics.Palette
import com.clouddroid.usagesafe.models.AppUsageInfo
import com.clouddroid.usagesafe.repositories.UsageStatsRepository
import com.clouddroid.usagesafe.utils.PackageInfoUtils.getResizedAppIcon
import com.clouddroid.usagesafe.utils.TextUtils.getTotalScreenTimeText
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class TodaysStatsViewModel @Inject constructor(private val usageStatsRepository: UsageStatsRepository) : ViewModel() {

    private val appUsageMap = MutableLiveData<Map<String, AppUsageInfo>>()
    val unlockCount = MutableLiveData<Int>()

    @SuppressLint("CheckResult")
    fun init() {
        Observable.fromCallable { usageStatsRepository.getAppsUsageFromToday() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val usagePair = it
                appUsageMap.value = usagePair.first
                unlockCount.value = usagePair.second
            }, {
                it.printStackTrace()
            })

    }

    fun getAppUsageMap(): LiveData<Map<String, AppUsageInfo>> = Transformations.map(appUsageMap) {
        it.toList()
            .sortedByDescending { (_, value) ->
                value.totalTimeInForeground
            }
            .toMap()
    }

    fun getTotalScreenTime(appUsageMap: Map<String, AppUsageInfo>, context: Context?): String {
        return getTotalScreenTimeText(
            appUsageMap.toList().sumBy { it.second.totalTimeInForeground.toInt() }.toLong()
            , context
        )
    }

    fun getMostUsedAppsList(appUsageMap: Map<String, AppUsageInfo>): List<AppUsageInfo> {
        return appUsageMap.toList().take(5).toMap().values.toList()
    }

    fun prepareEntriesForPieChart(appUsageMap: Map<String, AppUsageInfo>, context: Context?): List<PieEntry> {
        val firstAppsUsageMap = getFirstNonZeroAppsUsage(appUsageMap)
        val restAppsUsageMap = getOtherAppsUsage(appUsageMap, firstAppsUsageMap.size)
        return getPieEntriesFromAppsUsage(firstAppsUsageMap, restAppsUsageMap, context)
    }

    private fun getFirstNonZeroAppsUsage(appUsageMap: Map<String, AppUsageInfo>): Map<String, AppUsageInfo> {
        val firstAppsUsageMap = mutableMapOf<String, AppUsageInfo>()
        appUsageMap.toList().forEachIndexed { index, pair ->
            if (index < 5 && pair.second.totalTimeInForeground > 0) {
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
                    getResizedAppIcon(packageName, context)
                )
            )
        }

        val totalOtherTime = otherAppsUsageMap.toList().sumBy { it.second.totalTimeInForeground.toInt() }

        if (totalOtherTime > 0 && (totalOtherTime.div(firstAppsTotalTimeSum.toDouble()) * 100) > 10) {
            entries.add(
                PieEntry(
                    totalOtherTime.toFloat(),
                    "Other"
                )
            )
        }

        if (totalOtherTime > 0 && (totalOtherTime.div(firstAppsTotalTimeSum.toDouble()) * 100) <= 10) {
            entries.add(
                PieEntry(
                    totalOtherTime.toFloat(),
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
}