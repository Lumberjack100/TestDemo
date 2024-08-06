package com.shmedo.core.data.extensions

import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.TimeUtils

import com.shmedo.core.commonlib.utils.MmkvCacheUtil
import com.shmedo.core.data.source.local.entity.LogItem
import com.shmedo.core.data.source.local.entity.LogSession
import java.util.UUID

/**
 * 创建者：gonghe
 * 创建时间：2023/12/29
 * 描述： TODO
 */
fun getSystemLogSession(): LogSession {
    val session = LogSession(
        id = UUID.randomUUID().toString(),
        key = "V " + AppUtils.getAppVersionName(),
        name = "系统日志",
        createBy = MmkvCacheUtil.getAccount(),
        createDate = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("yyyy-MM-dd")),
        createTime = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("HH:mm")),
    )
    MmkvCacheUtil.setAppLogSessionId(session.id)
    return session
}

fun getIOTDeviceLogSession(mKey: String, mName: String): LogSession {
    val session = LogSession(
        id = UUID.randomUUID().toString(),
        key = mKey,
        name = mName,
        createBy = MmkvCacheUtil.getAccount(),
        createDate = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("yyyy-MM-dd")),
        createTime = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("HH:mm")),
    )
    MmkvCacheUtil.setIOTDeviceLogSessionId(session.id)
    return session
}

fun getLogItem(sessionId: String, priority: Int, data: String): LogItem {
    return LogItem(
        id = UUID.randomUUID().toString(),
        sessionId = sessionId,
        logLevel = priority,
        data = data,
        createDate = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("yyyy-MM-dd")),
        createTime = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("HH:mm:ss.SSS")),
    )
}
