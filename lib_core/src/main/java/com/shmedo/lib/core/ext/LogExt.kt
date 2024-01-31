package com.shmedo.lib.core.ext

import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.TimeUtils
import com.shmedo.lib.core.base.model.LogInfo
import com.shmedo.lib.core.base.model.LogLevel
import com.shmedo.lib.core.base.model.SessionInfo
import com.shmedo.lib.core.data.repository.LocalDataRepository
import com.shmedo.lib.core.util.MmkvCacheUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2023/12/29
 * 描述： TODO
 */

fun addSystemLogSession(scope: CoroutineScope) {
    scope.launch {
        val sessionInfo = SessionInfo(
            key = "V " + AppUtils.getAppVersionName(),
            name = "系统日志",
            createBy = MmkvCacheUtil.getUserName(),
            createDate = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("yyyy-MM-dd")),
            createTime = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("HH:mm")),
        )
        LocalDataRepository.instance.insertSession(sessionInfo)
        MmkvCacheUtil.setAppLogSessionId(sessionInfo.id)
    }
}

fun addSystemLogItem(priority: Int, data: String, scope: CoroutineScope) {
    scope.launch {
        val logInfo = LogInfo(
            sessionId = MmkvCacheUtil.getAppLogSessionId(),
            logLevel = LogLevel.fromPriority(priority),
            data = data,
            createDate = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("yyyy-MM-dd")),
            createTime = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("HH:mm:ss.SSS")),
        )
        LocalDataRepository.instance.insertLog(logInfo)
    }
}

fun addIOTDeviceLogItem(priority: Int, data: String, scope: CoroutineScope) {
    scope.launch {
        val logInfo = LogInfo(
            sessionId = MmkvCacheUtil.getIOTDeviceLogSessionId(),
            logLevel = LogLevel.fromPriority(priority),
            data = data,
            createDate = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("yyyy-MM-dd")),
            createTime = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("HH:mm:ss.SSS")),
        )
        LocalDataRepository.instance.insertLog(logInfo)
    }
}


/**
 * 控制台输出带协程信息的log
 */
fun logX(any: Any?) {
    Timber.d(
        """
================================
$any
${TimeUtils.getNowString()} Thread:${Thread.currentThread().name}
""".trimIndent()
    )
}