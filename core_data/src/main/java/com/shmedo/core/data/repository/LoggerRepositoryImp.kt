package com.shmedo.core.data.repository

import com.blankj.utilcode.util.TimeUtils
import com.shmedo.core.data.source.local.AppDatabase
import com.shmedo.core.data.source.local.entity.LogItem
import com.shmedo.core.data.source.local.entity.LogSession

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
class LoggerRepositoryImp(private val database: AppDatabase) {

    //<editor-fold desc="日志会话信息">
    suspend fun getSessionListByUser(account: String): List<LogSession> =
        withContext(Dispatchers.IO) {
            database.sessionInfoDao().getSessionListByUser(account)
        }

    //查询当前用户下当天内 name 相同的会话
    suspend fun getSessionByUserAndName(
        account: String,
        name: String,
        createDate: String
    ): List<LogSession> = withContext(Dispatchers.IO) {
        database.sessionInfoDao().getSessionByUserAndName(account, name, createDate)
    }

    suspend fun getSessionById(id: String): LogSession? = withContext(Dispatchers.IO) {
        database.sessionInfoDao().getSessionById(id)
    }

    suspend fun insertSession(info: LogSession) = withContext(Dispatchers.IO) {
        database.sessionInfoDao().insertSession(info)
    }

    suspend fun insertSessionList(list: List<LogSession>) = withContext(Dispatchers.IO) {
        database.sessionInfoDao().insertSessionList(list)
    }

    suspend fun deleteSessionById(id: String) = withContext(Dispatchers.IO) {
        database.sessionInfoDao().deleteById(id)
    }
    // </editor-fold>


    //<editor-fold desc="日志数据信息">
    suspend fun getLogListBySessionId(
        sessionId: String,
        level: Int
    ): List<LogItem> = withContext(Dispatchers.IO) {
        database.logInfoDao().getLogListBySessionId(sessionId, level)
    }

    suspend fun insertLog(info: LogItem) = withContext(Dispatchers.IO) {
        database.logInfoDao().insertLog(info)
    }

    suspend fun insertLogList(list: List<LogItem>) = withContext(Dispatchers.IO) {
        database.logInfoDao().insertLogList(list)
    }
    // </editor-fold>

    /**
     * 清除历史日志,保留当天的日志
     */
    /**
     * 清除历史日志,保留当天的日志
     */
    suspend fun clearHistoryLog() = withContext(Dispatchers.IO) {
        database.logInfoDao()
            .clearHistoryData(TimeUtils.getNowString(TimeUtils.getSafeDateFormat("yyyy-MM-dd")))
        database.sessionInfoDao()
            .clearHistoryData(TimeUtils.getNowString(TimeUtils.getSafeDateFormat("yyyy-MM-dd")))
    }
}