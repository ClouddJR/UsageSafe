package com.clouddroid.usagesafe.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.clouddroid.usagesafe.data.model.AppLimit
import com.clouddroid.usagesafe.data.model.FocusModeApp
import com.clouddroid.usagesafe.data.model.GroupLimit
import com.clouddroid.usagesafe.data.model.LogEvent

@Database(
    entities = [LogEvent::class, AppLimit::class, GroupLimit::class, FocusModeApp::class],
    version = 2
)
abstract class LocalDatabase : RoomDatabase() {

    abstract fun logEventDao(): LogEventDao
    abstract fun appLimitDao(): AppLimitDao
    abstract fun groupLimitDao(): GroupLimitDao
    abstract fun focusModeAppDao(): FocusModeAppDao

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
                .addMigrations(MIGRATION_1_2)
                .allowMainThreadQueries()
                .build()

        //add a new field (id) as a primary key instead of timestamp
        //because different logs can exist with the same timestamp value
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE logevents_new (" +
                        "id INTEGER NOT NULL, " +
                        "timestamp INTEGER NOT NULL, " +
                        "packagename TEXT NOT NULL, " +
                        "classname TEXT, " +
                        "type INTEGER NOT NULL," +
                        "PRIMARY KEY(id))")
                database.execSQL("INSERT INTO logevents_new (timestamp, packagename, classname, type) " +
                        "SELECT timestamp, packagename, classname, type FROM logevents")
                database.execSQL("DROP TABLE logevents")
                database.execSQL("ALTER TABLE logevents_new RENAME TO logevents")
            }
        }
    }
}