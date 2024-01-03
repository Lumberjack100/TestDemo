package com.shmedo.lib.core.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shmedo.lib.core.base.model.SessionInfo

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/27
 *
 * 描述： TODO
 *
 *
 */
@Dao
interface SessionInfoDao {
    @Query("select * from sessions  where create_by = :userId")
    fun getSessionListByUserLiveData(userId: String): LiveData<List<SessionInfo>?>

    @Query("select * from sessions  where create_by = :userId")
    suspend fun getSessionListByUser(userId: String): List<SessionInfo>?

    @Query("select * from sessions  where id = :id")
    suspend fun getSessionById(id: String): SessionInfo?

    @Query("select * from sessions")
    suspend fun getAllSessionList(): List<SessionInfo>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(record: SessionInfo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionList(list: List<SessionInfo>)

    @Query("DELETE FROM sessions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM sessions WHERE create_date != :excludeDate")
    suspend fun clearHistoryData(excludeDate: String)
}