package com.clouddroid.usagesafe.data.model

import android.graphics.drawable.Drawable

data class AppDetails(
    var name: String = "",
    var packageName: String = "",
    var icon: Drawable?,
    var isSystemApp: Boolean
)