package com.clouddroid.usagesafe.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.clouddroid.usagesafe.data.model.LogEvent

@Dao
interface LogEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLogEvents(logEvents: List<LogEvent>)

    @Query("SELECT * FROM logevents WHERE timestamp BETWEEN :begin AND :end")
    fun getLogEventsBetweenRange(begin: Long, end: Long): List<LogEvent>

    @Query("SELECT * FROM logevents WHERE timestamp LIKE (SELECT MIN(timestamp) FROM logevents) LIMIT 1")
    fun getTheEarliestLogEvent(): LogEvent?

    @Query("DELETE FROM logevents WHERE timestamp BETWEEN :begin AND :end")
    fun deleteLogEventsBetweenRange(begin: Long, end: Long)
}