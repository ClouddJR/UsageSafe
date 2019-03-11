package com.clouddroid.usagesafe.utils

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable

object PackageInfoUtils {

    fun getAppIcon(packageName: String, context: Context?): BitmapDrawable? {
        val drawable = context?.packageManager?.getApplicationIcon(packageName)
        val bitmap = drawable?.toBitmap(80, 80)
        return bitmap?.toDrawable(context.resources)
    }
}