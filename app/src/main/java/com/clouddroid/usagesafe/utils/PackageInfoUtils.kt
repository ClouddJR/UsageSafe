package com.clouddroid.usagesafe.utils

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import com.clouddroid.usagesafe.R

object PackageInfoUtils {

    fun getResizedAppIcon(packageName: String, context: Context?): Drawable? {
        return try {
            val drawable = context?.packageManager?.getApplicationIcon(packageName)
            val bitmap = drawable?.toBitmap(80, 80)
            bitmap?.toDrawable(context.resources)
        } catch (e: PackageManager.NameNotFoundException) {
            val defaultDrawable = context?.getDrawable(R.mipmap.ic_launcher_round)
            val bitmap = defaultDrawable?.toBitmap(80, 80)
            bitmap?.toDrawable(context.resources)
        }
    }

    fun getRawAppIcon(packageName: String, context: Context?): Drawable? {
        return try {
            return context?.packageManager?.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            context?.getDrawable(R.mipmap.ic_launcher_round)
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
}