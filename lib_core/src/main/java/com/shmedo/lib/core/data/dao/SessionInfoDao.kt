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
    @Query("select * from sessions  where create_by = :account")
    fun getSessionListByUserLiveData(account: String): LiveData<List<SessionInfo>?>

    @Query("select * from sessions  where create_by = :account")
    suspend fun getSessionListByUser(account: String): List<SessionInfo>

    //查询当前用户下当天内 name 相同的会话
    @Query("select * from sessions  where create_by = :account and name = :name and create_date = :createDate")
    suspend fun getSessionByUserAndName(
        account: String,
        name: String,
        createDate: String
    ):  List<SessionInfo>

    @Query("select * from sessions  where id = :id")
    suspend fun getSessionById(id: String): SessionInfo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(record: SessionInfo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionList(list: List<SessionInfo>)

    @Query("DELETE FROM sessions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM sessions WHERE create_date != :excludeDate")
    suspend fun clearHistoryData(excludeDate: String)
}