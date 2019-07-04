package com.clouddroid.usagesafe.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_mode_apps")
data class FocusModeApp(

    @PrimaryKey
    @ColumnInfo(name = "package_name")
    var packageName: String = ""
)