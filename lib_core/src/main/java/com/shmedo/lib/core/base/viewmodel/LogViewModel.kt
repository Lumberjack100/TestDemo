package com.shmedo.lib.core.base.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.TimeUtils
import com.shmedo.lib.core.base.model.LogInfo
import com.shmedo.lib.core.base.model.SessionInfo
import com.shmedo.lib.core.data.repository.LoggerRepositoryImp
import com.shmedo.lib.core.ext.getIOTDeviceLogSession
import com.shmedo.lib.core.ext.getLogItem
import com.shmedo.lib.core.ext.getSystemLogSession
import com.shmedo.lib.core.util.LogHelper
import com.shmedo.lib.core.util.MmkvCacheUtil
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

    /**
     * 插入会话信息,并返回 sessionId
     * 如果当前用户下当天内 name 相同的会话不存在,则插入，否则不插入，并
     */
    fun insertSystemLogSession() = viewModelScope.launch {
        val sessionList = loggerRepositoryImp.getSessionByUserAndName(
            MmkvCacheUtil.getAccount(),
            "系统日志",
            TimeUtils.getNowString(TimeUtils.getSafeDateFormat("yyyy-MM-dd"))
        )
        if (sessionList.isEmpty()) {
            val newSession = getSystemLogSession()
            loggerRepositoryImp.insertSession(newSession)
            loggerRepositoryImp.insertLog(
                getLogItem(
                    sessionId = MmkvCacheUtil.getAppLogSessionId(),
                    priority = Log.INFO,
                    data = LogHelper.printDeviceInfo()
                )
            )
        } else {
            MmkvCacheUtil.setAppLogSessionId(sessionList[0].id)
        }
    }

    /**
     * 插入会话信息,并返回 sessionId
     * 如果当前用户下当天内 name 相同的会话不存在,则插入，否则不插入，并
     */
    fun insertIOTDeviceLogSession(mKey: String, mName: String) =
        viewModelScope.launch {
            val sessionList = loggerRepositoryImp.getSessionByUserAndName(
                MmkvCacheUtil.getAccount(),
                mName,
                TimeUtils.getNowString(TimeUtils.getSafeDateFormat("yyyy-MM-dd"))
            )
            if (sessionList.isEmpty()) {
                val newSession = getIOTDeviceLogSession(mKey, mName)
                loggerRepositoryImp.insertSession(newSession)
            } else {
                MmkvCacheUtil.setIOTDeviceLogSessionId(sessionList[0].id)
            }
        }


    suspend fun getLogListBySessionId(
        sessionId: String, level: Int = Log.DEBUG
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