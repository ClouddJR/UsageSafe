package com.clouddroid.usagesafe.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.clouddroid.usagesafe.data.model.FocusModeApp
import io.reactivex.Flowable

@Dao
interface FocusModeAppDao {

    @Query("SELECT * FROM focus_mode_apps")
    fun getAllFocusModeApps(): Flowable<List<FocusModeApp>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFocusModeApp(focusModeApp: FocusModeApp)

    @Query("DELETE FROM focus_mode_apps WHERE package_name = :packageName")
    fun deleteFocusModeApp(packageName: String)
}