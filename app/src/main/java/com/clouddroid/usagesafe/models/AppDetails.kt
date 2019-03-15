package com.clouddroid.usagesafe.models

import android.graphics.drawable.Drawable

data class AppDetails(
    var name: String = "",
    var packageName: String = "",
    var icon: Drawable?
)