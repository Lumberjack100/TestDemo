package com.shmedo.lib.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shmedo.lib.core.base.model.LogInfo

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
interface LogInfoDao {

    @Query("select * from log_info  where session_id = :session_id")
    suspend fun getLogListBySessionId(session_id: String): List<LogInfo>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(record: LogInfo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogList(list: List<LogInfo>)

    @Query("DELETE FROM log_info WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM log_info WHERE session_id = :session_id")
    suspend fun deleteBySessionId(session_id: String)
}