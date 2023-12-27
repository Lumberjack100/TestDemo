package com.shmedo.lib.core.base.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shmedo.lib.core.base.model.LogInfo
import com.shmedo.lib.core.base.model.SessionInfo
import com.shmedo.lib.core.data.repository.LocalDataRepository
import kotlinx.coroutines.launch

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/27
 *
 * 描述： TODO
 *
 *
 */
class LogViewModel : ViewModel() {
    private var logSession: SessionInfo? = null
    fun getLogSession(): SessionInfo? {
        return logSession
    }

    suspend fun getSessionListByUser(
        userId: String
    ): List<SessionInfo>? = LocalDataRepository.instance.getSessionListByUser(userId)

    fun insertSession(info: SessionInfo) = viewModelScope.launch {
        logSession = info
        LocalDataRepository.instance.insertSession(info)
    }

    fun deleteSessionById(primaryId: String) = viewModelScope.launch {
        LocalDataRepository.instance.deleteSessionById(primaryId)
    }


    fun insertLog(info: LogInfo) = viewModelScope.launch {
        LocalDataRepository.instance.insertLog(info)
    }

    fun insertLogList(list: List<LogInfo>) = viewModelScope.launch {
        LocalDataRepository.instance.insertLogList(list)
    }
}