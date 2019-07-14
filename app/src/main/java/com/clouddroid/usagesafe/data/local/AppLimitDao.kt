package com.clouddroid.usagesafe.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.clouddroid.usagesafe.data.model.AppLimit
import io.reactivex.Flowable

@Dao
interface AppLimitDao {

    @Query("SELECT * FROM app_limits")
    fun getAllLimits(): Flowable<List<AppLimit>>

    @Query("SELECT COUNT(*) FROM app_limits")
    fun getNumberOfAppLimits(): Flowable<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAppLimit(appLimit: AppLimit)

    @Query("DELETE FROM app_limits WHERE packagename = :packageName")
    fun deleteAppLimit(packageName: String)
}