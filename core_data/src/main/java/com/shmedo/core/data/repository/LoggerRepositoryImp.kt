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

    suspend fun getLogSessionList(account: String): List<LogSession> =
        withContext(Dispatchers.IO) {
            database.logSessionDao().getLogSessionList(account)
        }

    //查询当前用户下当天内 name 相同的会话
    suspend fun getLogSessionListByName(
        account: String,
        name: String,
        createDate: String
    ): List<LogSession> = withContext(Dispatchers.IO) {
        database.logSessionDao().getLogSessionListByName(account, name, createDate)
    }

    suspend fun insertLogSession(info: LogSession) = withContext(Dispatchers.IO) {
        database.logSessionDao().insert(info)
    }

    suspend fun deleteLogSessionById(id: String) = withContext(Dispatchers.IO) {
        database.logSessionDao().deleteById(id)
    }

    suspend fun getLogItemListBySessionId(
        sessionId: String,
        level: Int
    ): List<LogItem> = withContext(Dispatchers.IO) {
        database.logItemDao().getLogItemListBySessionId(sessionId, level)
    }

    suspend fun insertLogItem(info: LogItem) = withContext(Dispatchers.IO) {
        database.logItemDao().insert(info)
    }

    suspend fun batchDeleteLogItemBySessionId(id: String) = withContext(Dispatchers.IO) {
        database.logItemDao().batchDeleteBySessionId(id)
    }

    /**
     * 清除历史日志,保留当天的日志
     */
    suspend fun clearHistoryLog() = withContext(Dispatchers.IO) {
        database.logItemDao()
            .clearHistoryData(TimeUtils.getNowString(TimeUtils.getSafeDateFormat("yyyy-MM-dd")))
        database.logSessionDao()
            .clearHistoryData(TimeUtils.getNowString(TimeUtils.getSafeDateFormat("yyyy-MM-dd")))
    }
}