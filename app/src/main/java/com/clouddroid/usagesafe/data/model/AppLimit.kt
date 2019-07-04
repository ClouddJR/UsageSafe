package com.clouddroid.usagesafe.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_limits")
data class AppLimit(

    @PrimaryKey
    @ColumnInfo(name = "packagename")
    var packageName: String = "",

    @ColumnInfo(name = "limit")
    var limit: Long = 0,

    @ColumnInfo(name = "group_id")
    var groupId: Int = 0
)
