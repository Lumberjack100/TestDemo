package com.shmedo.core.model

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

/**
 * 监测数据查询请求
 */
@JsonClass(generateAdapter = true)
@Parcelize
data class MonitoringDataQuery(
    val beginTime: String,      // 格式: yyyyMMddHHmmss
    val endTime: String,        // 格式: yyyyMMddHHmmss
    val apiKey: String,
    val msgId: String
) : Parcelable

/**
 * 监测数据查询响应
 */
@JsonClass(generateAdapter = true)
@Parcelize
data class MonitoringDataResponse(
    val currentPageData: List<MonitoringDataItem>,
    val result: Boolean,         //
    val reason: String? = null, // 失败原因
    val readEnd: Boolean = false // 是否已读取完毕
) : Parcelable

/**
 * 单条监测数据
 */
@JsonClass(generateAdapter = true)
@Parcelize
data class MonitoringDataItem(
    val timeStr: String,        // 时间字符串
    val content: String         // JSON格式的传感器数据
) : Parcelable

/**
 * 数据传输状态
 */
sealed class TransferState {
    object Idle : TransferState()
    data class Transferring(
        val progress: Float,
        val speed: String,                  // 传输速度
        val transferredDataSize: String     // 已传输数据大小
    ) : TransferState()

    data class Success(val totalCount: Int) : TransferState()
    data class Error(val message: String) : TransferState()
} 