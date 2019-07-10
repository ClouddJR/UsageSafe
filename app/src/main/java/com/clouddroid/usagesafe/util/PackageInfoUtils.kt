package com.clouddroid.usagesafe.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import com.clouddroid.usagesafe.BuildConfig
import java.util.*


object PackageInfoUtils {

    fun getResizedAppIcon(packageName: String, context: Context?): Drawable? {
        return try {
            val drawable = context?.packageManager?.getApplicationIcon(packageName)
            val bitmap = drawable?.toBitmap(80, 80)
            bitmap?.toDrawable(context.resources)
        } catch (e: PackageManager.NameNotFoundException) {
            val defaultDrawable = context?.getDrawable(com.clouddroid.usagesafe.R.mipmap.ic_launcher_round)
            val bitmap = defaultDrawable?.toBitmap(80, 80)
            bitmap?.toDrawable(context.resources)
        }
    }

    fun getRawAppIcon(packageName: String, context: Context?): Drawable? {
        return try {
            return context?.packageManager?.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            context?.getDrawable(com.clouddroid.usagesafe.R.mipmap.ic_launcher_round)
        }
    }

    fun getAppName(packageName: String, context: Context?): CharSequence? {
        return try {
            val applicationInfo = context?.packageManager?.getApplicationInfo(packageName, 0)
            if (applicationInfo != null) context.packageManager?.getApplicationLabel(applicationInfo)
            else packageName
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }

    fun getAppName(packageName: String, packageManager: PackageManager): CharSequence? {
        return try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            if (applicationInfo != null) packageManager.getApplicationLabel(applicationInfo)
            else packageName
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }

    fun getDefaultLauncherPackageName(packageManager: PackageManager): String {
        val intent = Intent("android.intent.action.MAIN").apply {
            addCategory("android.intent.category.HOME")
        }

        return packageManager.resolveActivity(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        )?.activityInfo?.packageName ?: ""
    }

    fun getLaunchableAppList(packageManager: PackageManager): List<ResolveInfo> {
        val main = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val launchables = packageManager.queryIntentActivities(main, 0)
        launchables.removeAll { it.activityInfo.packageName == BuildConfig.APPLICATION_ID }

        Collections.sort(
            launchables,
            ResolveInfo.DisplayNameComparator(packageManager)
        )

        return launchables
    }
}