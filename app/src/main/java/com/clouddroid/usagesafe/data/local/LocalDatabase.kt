package com.clouddroid.usagesafe.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.clouddroid.usagesafe.data.model.AppLimit
import com.clouddroid.usagesafe.data.model.LogEvent

@Database(entities = [LogEvent::class, AppLimit::class], version = 1)
abstract class LocalDatabase : RoomDatabase() {

    abstract fun logEventDao(): LogEventDao
    abstract fun appLimitDao(): AppLimitDao

    companion object {

        @Volatile
        private var INSTANCE: LocalDatabase? = null

        fun getInstance(context: Context): LocalDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                LocalDatabase::class.java, "UsageData.db"
            )
                .allowMainThreadQueries()
                .build()
    }
}