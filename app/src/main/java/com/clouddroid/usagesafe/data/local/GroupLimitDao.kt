package com.clouddroid.usagesafe.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.clouddroid.usagesafe.data.model.GroupLimit
import io.reactivex.Flowable

@Dao
interface GroupLimitDao {

    @Query("SELECT * FROM group_limits")
    fun getAllGroupLimits(): Flowable<List<GroupLimit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGroupLimit(groupLimit: GroupLimit)

    @Query("DELETE FROM group_limits WHERE group_id = :groupId")
    fun deleteGroupLimit(groupId: String)
}