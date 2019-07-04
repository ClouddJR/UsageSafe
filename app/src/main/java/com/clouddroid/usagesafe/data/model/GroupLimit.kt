package com.clouddroid.usagesafe.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "group_limits")
data class GroupLimit(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "group_id")
    var groupId: Int = 0,

    @ColumnInfo(name = "limit")
    var limit: Long = 0
)