package com.shmedo.lib.core.data.repository

import com.shmedo.lib.core.base.model.LogInfo
import com.shmedo.lib.core.base.model.SessionInfo
import com.shmedo.lib.core.data.dao.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/27
 *
 * 描述： TODO
 *
 *
 */
class LocalDataRepository private constructor() {

    //<editor-fold desc="日志会话信息">
    suspend fun getAllSessionList(): List<SessionInfo>? =
        withContext(Dispatchers.IO) {
            AppDatabase.INSTANCE.sessionInfoDao().getAllSessionList()
        }

    suspend fun getSessionListByUser(userId: String): List<SessionInfo>? =
        withContext(Dispatchers.IO) {
            AppDatabase.INSTANCE.sessionInfoDao().getSessionListByUser(userId)
        }

    suspend fun getSessionById(id: String): SessionInfo? =
        withContext(Dispatchers.IO) {
            AppDatabase.INSTANCE.sessionInfoDao().getSessionById(id)
        }

    suspend fun insertSession(info: SessionInfo) = withContext(Dispatchers.IO) {
        AppDatabase.INSTANCE
            .sessionInfoDao()
            .insertSession(info)
    }

    suspend fun insertSessionList(list: List<SessionInfo>) =
        withContext(Dispatchers.IO) {
            AppDatabase.INSTANCE
                .sessionInfoDao()
                .insertSessionList(list)
        }

    suspend fun deleteSessionById(id: String) = withContext(Dispatchers.IO) {
        AppDatabase.INSTANCE.sessionInfoDao()
            .deleteById(id)
    }
    // </editor-fold>


    //<editor-fold desc="日志数据信息">
    suspend fun getLogListBySessionId(session_id: String): List<LogInfo>? =
        withContext(Dispatchers.IO) {
            AppDatabase.INSTANCE.logInfoDao().getLogListBySessionId(session_id)
        }

    suspend fun insertLog(info: LogInfo) = withContext(Dispatchers.IO) {
        AppDatabase.INSTANCE
            .logInfoDao()
            .insertLog(info)
    }

    suspend fun insertLogList(list: List<LogInfo>) =
        withContext(Dispatchers.IO) {
            AppDatabase.INSTANCE
                .logInfoDao()
                .insertLogList(list)
        }

    suspend fun deleteLogById(id: String) = withContext(Dispatchers.IO) {
        AppDatabase.INSTANCE.logInfoDao()
            .deleteById(id)
    }

    suspend fun deleteLogBySessionId(session_id: String) = withContext(Dispatchers.IO) {
        AppDatabase.INSTANCE.logInfoDao()
            .deleteBySessionId(session_id)
    }
    // </editor-fold>

    companion object {
        val instance = LocalDataRepository()
    }
}