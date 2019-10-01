package com.clouddroid.usagesafe.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "logevents")
data class LogEvent (

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    @ColumnInfo(name = "timestamp")
    var timestamp: Long = 0,

    @ColumnInfo(name = "packagename")
    var packageName: String = "",

    @ColumnInfo(name = "classname")
    var className: String? = "",

    @ColumnInfo(name = "type")
    var type: Int = 0
)