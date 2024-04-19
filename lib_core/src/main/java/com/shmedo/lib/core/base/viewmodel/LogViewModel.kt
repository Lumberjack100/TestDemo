package com.shmedo.lib.core.base.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shmedo.lib.core.base.model.LogInfo
import com.shmedo.lib.core.base.model.LogLevel
import com.shmedo.lib.core.base.model.SessionInfo
import com.shmedo.lib.core.data.repository.LoggerRepositoryImp
import kotlinx.coroutines.launch

/**
 * 创建者：gonghe
 * 创建时间：2023/12/27
 * 描述： TODO
 */
class LogViewModel(private val loggerRepositoryImp: LoggerRepositoryImp) : ViewModel() {
    suspend fun getSessionListByUser(
        userId: String
    ): List<SessionInfo> = loggerRepositoryImp.getSessionListByUser(userId)

    fun insertSession(info: SessionInfo) = viewModelScope.launch {
        loggerRepositoryImp.insertSession(info)
    }

    fun deleteSessionById(primaryId: String) = viewModelScope.launch {
        loggerRepositoryImp.deleteSessionById(primaryId)
    }

    suspend fun getLogListBySessionId(
        sessionId: String, level: Int = LogLevel.DEBUG
    ): List<LogInfo> = loggerRepositoryImp.getLogListBySessionId(sessionId, level)

    fun insertLog(info: LogInfo) = viewModelScope.launch {
        loggerRepositoryImp.insertLog(info)
    }

    fun insertLogList(list: List<LogInfo>) = viewModelScope.launch {
        loggerRepositoryImp.insertLogList(list)
    }

    /**
     * 清除历史日志,保留当天的日志
     */
    fun clearHistoryLog() = viewModelScope.launch {
        loggerRepositoryImp.clearHistoryLog()
    }
}