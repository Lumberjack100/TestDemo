package com.shmedo.core.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shmedo.core.data.source.local.entity.LogItem

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
interface LogItemDao {

    @Query("select * from log_info  where session_id = :sessionId and log_level>= :level")
    suspend fun getLogItemListBySessionId(sessionId: String, level: Int): List<LogItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: LogItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun batchInsert(list: List<LogItem>)

    @Query("DELETE FROM log_info WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM log_info WHERE session_id = :sessionId")
    suspend fun batchDeleteBySessionId(sessionId: String)

    @Query("DELETE FROM log_info WHERE create_date != :excludeDate")
    suspend fun clearHistoryData(excludeDate: String)
}