package com.clouddroid.usagesafe.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.clouddroid.usagesafe.data.model.AppLimit
import io.reactivex.Flowable

@Dao
interface AppLimitDao {

    @Query("SELECT * FROM applimits")
    fun getAllLimits(): Flowable<List<AppLimit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAppLimit(appLimit: AppLimit)

    @Query("DELETE FROM applimits WHERE packagename = :packageName")
    fun deleteAppLimit(packageName: String)
}