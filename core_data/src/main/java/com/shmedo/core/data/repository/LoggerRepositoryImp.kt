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
    suspend fun getLogSessionListByCurrentUser(account: String): List<LogSession> =
        withContext(Dispatchers.IO) {
            database.sessionInfoDao().getLogSessionList(account)
        }

    //查询当前用户下当天内 name 相同的会话
    suspend fun getLogSessionListByName(
        account: String,
        name: String,
        createDate: String
    ): List<LogSession> = withContext(Dispatchers.IO) {
        database.sessionInfoDao().getLogSessionListByName(account, name, createDate)
    }

    suspend fun insertLogSession(info: LogSession) = withContext(Dispatchers.IO) {
        database.sessionInfoDao().insert(info)
    }

    suspend fun deleteLogSessionById(id: String) = withContext(Dispatchers.IO) {
        database.sessionInfoDao().deleteById(id)
    }
    // </editor-fold>


    //<editor-fold desc="日志数据信息">
    suspend fun getLogItemListBySessionId(
        sessionId: String,
        level: Int
    ): List<LogItem> = withContext(Dispatchers.IO) {
        database.logInfoDao().getLogItemListBySessionId(sessionId, level)
    }

    suspend fun insertLogItem(info: LogItem) = withContext(Dispatchers.IO) {
        database.logInfoDao().insert(info)
    }

    suspend fun batchInsertLogItem(list: List<LogItem>) = withContext(Dispatchers.IO) {
        database.logInfoDao().batchInsert(list)
    }

    suspend fun batchDeleteLogItemBySessionId(id: String) = withContext(Dispatchers.IO) {
        database.sessionInfoDao().deleteById(id)
    }
    // </editor-fold>


    /**
     * 清除历史日志,保留当天的日志
     */
    suspend fun clearHistoryLog() = withContext(Dispatchers.IO) {
        database.logInfoDao()
            .batchDelete(TimeUtils.getNowString(TimeUtils.getSafeDateFormat("yyyy-MM-dd")))
        database.sessionInfoDao()
            .batchDelete(TimeUtils.getNowString(TimeUtils.getSafeDateFormat("yyyy-MM-dd")))
    }
}