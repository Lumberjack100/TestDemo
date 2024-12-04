package com.shmedo.mcloudapp.ui.viewmodel.state

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.TimeUtils
import com.shmedo.core.commonlib.mmkv.CommonMMKVOwner
import com.shmedo.core.commonlib.mmkv.MmkvCacheUtil
import com.shmedo.core.commonlib.utils.LogHelper
import com.shmedo.core.data.extensions.getIOTDeviceLogSession
import com.shmedo.core.data.extensions.getLogItem
import com.shmedo.core.data.extensions.getSystemLogSession
import com.shmedo.core.data.repository.LoggerRepositoryImp
import com.shmedo.core.data.source.local.entity.LogItem
import com.shmedo.core.data.source.local.entity.LogSession

import kotlinx.coroutines.launch

/**
 * 创建者：gonghe
 * 创建时间：2023/12/27
 * 描述： TODO
 */
class LogViewModel(private val loggerRepositoryImp: LoggerRepositoryImp) : ViewModel() {

    /**
     * 插入会话信息,并返回 sessionId
     * 如果当前用户下当天内 name 相同的会话不存在,则插入，否则不插入，并
     */
    fun insertSystemLogSession() = viewModelScope.launch {
        val sessionList = loggerRepositoryImp.getLogSessionListByName(
            MmkvCacheUtil.getAccount(),
            "系统日志",
            TimeUtils.getNowString(TimeUtils.getSafeDateFormat("yyyy-MM-dd"))
        )
        if (sessionList.isEmpty()) {
            val newSession = getSystemLogSession()
            loggerRepositoryImp.insertLogSession(newSession)
            loggerRepositoryImp.insertLogItem(
                getLogItem(
                    sessionId = CommonMMKVOwner.appLogSessionId,
                    priority = Log.INFO,
                    data = LogHelper.printDeviceInfo()
                )
            )
        } else {
            CommonMMKVOwner.appLogSessionId = sessionList[0].id
        }
    }

    /**
     * 插入会话信息,并返回 sessionId
     * 如果当前用户下当天内 name 相同的会话不存在,则插入，否则不插入，并
     */
    fun insertIOTDeviceLogSession(mKey: String, mName: String) =
        viewModelScope.launch {
            val sessionList = loggerRepositoryImp.getLogSessionListByName(
                MmkvCacheUtil.getAccount(),
                mName,
                TimeUtils.getNowString(TimeUtils.getSafeDateFormat("yyyy-MM-dd"))
            )
            if (sessionList.isEmpty()) {
                val newSession = getIOTDeviceLogSession(mKey, mName)
                loggerRepositoryImp.insertLogSession(newSession)
            } else {
                CommonMMKVOwner.iotDeviceLogSessionId = sessionList[0].id
            }
        }

    suspend fun getLogSessionList(
        userId: String
    ): List<LogSession> = loggerRepositoryImp.getLogSessionList(userId)


    suspend fun getLogItemListBySessionId(
        sessionId: String,
        level: Int = Log.DEBUG
    ): List<LogItem> = loggerRepositoryImp.getLogItemListBySessionId(sessionId, level)


    fun insertLogItem(info: LogItem) = viewModelScope.launch {
        loggerRepositoryImp.insertLogItem(info)
    }

    /**
     * 删除会话信息
     */
    fun deleteLogSessionById(id: String) = viewModelScope.launch {
        loggerRepositoryImp.deleteLogSessionById(id)
        loggerRepositoryImp.batchDeleteLogItemBySessionId(id)
    }

    /**
     * 清除历史日志,保留当天的日志
     */
    fun clearHistoryLog() = viewModelScope.launch {
        loggerRepositoryImp.clearHistoryLog()
    }
}