package com.shmedo.mcloudapp.utils

import com.shmedo.core.model.MonitoringDataQuery
import com.shmedo.core.model.TransferState
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 传输日志记录器
 */
object TransferLogger {
    
    private const val TAG = "DataTransfer"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    
    /**
     * 记录传输开始
     */
    fun logTransferStart(query: MonitoringDataQuery, deviceInfo: String) {
        val timestamp = dateFormat.format(Date())
        Timber.tag(TAG).i(
            "=== 传输开始 === [$timestamp]\n" +
            "设备信息: $deviceInfo\n" +
            "时间范围: ${query.beginTime} ~ ${query.endTime}\n" +
            "API密钥: ${query.apiKey.take(8)}...\n" +
            "消息ID: ${query.msgId}"
        )
    }
    
    /**
     * 记录传输进度
     */
    fun logTransferProgress(state: TransferState.Transferring, deviceSn: String) {
        val timestamp = dateFormat.format(Date())
        Timber.tag(TAG).d(
            "传输进度更新 [$timestamp]\n" +
            "设备: $deviceSn\n" +
            "已传输: ${state.transferredDataSize}\n" +
            "条数: ${state.transferredDataCount}\n" +
            "速度: ${state.speed}"
        )
    }
    
    /**
     * 记录传输完成
     */
    fun logTransferComplete(totalCount: Int, deviceSn: String, duration: Long) {
        val timestamp = dateFormat.format(Date())
        val durationStr = DataFormatUtils.formatTransferDuration(duration)
        
        Timber.tag(TAG).i(
            "=== 传输完成 === [$timestamp]\n" +
            "设备: $deviceSn\n" +
            "总条数: $totalCount\n" +
            "总耗时: $durationStr\n" +
            "状态: 成功"
        )
    }
    
    /**
     * 记录传输错误
     */
    fun logTransferError(error: String, deviceSn: String, duration: Long, context: String = "") {
        val timestamp = dateFormat.format(Date())
        val durationStr = DataFormatUtils.formatTransferDuration(duration)
        
        Timber.tag(TAG).e(
            "=== 传输失败 === [$timestamp]\n" +
            "设备: $deviceSn\n" +
            "错误: $error\n" +
            "耗时: $durationStr\n" +
            "上下文: $context"
        )
    }
    
    /**
     * 记录重试操作
     */
    fun logRetryAttempt(attempt: Int, maxRetries: Int, reason: String, deviceSn: String) {
        val timestamp = dateFormat.format(Date())
        Timber.tag(TAG).w(
            "重试操作 [$timestamp]\n" +
            "设备: $deviceSn\n" +
            "尝试: $attempt/$maxRetries\n" +
            "原因: $reason"
        )
    }
    
    /**
     * 记录数据保存
     */
    fun logDataSave(deviceSn: String, dataCount: Int, dataSize: Long, batchIndex: Int = 0) {
        val timestamp = dateFormat.format(Date())
        val sizeStr = DataFormatUtils.formatDataSize(dataSize)
        
        Timber.tag(TAG).d(
            "数据保存 [$timestamp]\n" +
            "设备: $deviceSn\n" +
            "批次: $batchIndex\n" +
            "条数: $dataCount\n" +
            "大小: $sizeStr"
        )
    }
    
    /**
     * 记录导出开始
     */
    fun logExportStart(deviceSn: String, timeRange: String, dataCount: Int) {
        val timestamp = dateFormat.format(Date())
        Timber.tag("DataExport").i(
            "=== 导出开始 === [$timestamp]\n" +
            "设备: $deviceSn\n" +
            "时间范围: $timeRange\n" +
            "数据条数: $dataCount"
        )
    }
    
    /**
     * 记录导出完成
     */
    fun logExportComplete(deviceSn: String, filePath: String, fileSize: Long, duration: Long) {
        val timestamp = dateFormat.format(Date())
        val durationStr = DataFormatUtils.formatTransferDuration(duration)
        val sizeStr = DataFormatUtils.formatDataSize(fileSize)
        
        Timber.tag("DataExport").i(
            "=== 导出完成 === [$timestamp]\n" +
            "设备: $deviceSn\n" +
            "文件路径: $filePath\n" +
            "文件大小: $sizeStr\n" +
            "耗时: $durationStr"
        )
    }
    
    /**
     * 记录导出失败
     */
    fun logExportError(deviceSn: String, error: String, duration: Long) {
        val timestamp = dateFormat.format(Date())
        val durationStr = DataFormatUtils.formatTransferDuration(duration)
        
        Timber.tag("DataExport").e(
            "=== 导出失败 === [$timestamp]\n" +
            "设备: $deviceSn\n" +
            "错误: $error\n" +
            "耗时: $durationStr"
        )
    }
    
    /**
     * 记录性能指标
     */
    fun logPerformanceMetrics(metrics: PerformanceMonitor.TransferMetrics) {
        val timestamp = dateFormat.format(Date())
        Timber.tag("Performance").i(
            "=== 性能指标 === [$timestamp]\n" +
            "设备: ${metrics.deviceSn}\n" +
            "总耗时: ${DataFormatUtils.formatTransferDuration(metrics.duration)}\n" +
            "总大小: ${DataFormatUtils.formatDataSize(metrics.totalBytes)}\n" +
            "总条数: ${metrics.totalCount}\n" +
            "平均速度: ${DataFormatUtils.formatTransferSpeed(metrics.averageSpeed)}\n" +
            "成功率: ${(metrics.successRate * 100).toInt()}%\n" +
            "错误次数: ${metrics.errorCount}\n" +
            "重试次数: ${metrics.retryCount}"
        )
    }
    
    /**
     * 记录系统信息
     */
    fun logSystemInfo() {
        val timestamp = dateFormat.format(Date())
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val usedMemory = totalMemory - freeMemory
        
        Timber.tag("System").d(
            "系统信息 [$timestamp]\n" +
            "最大内存: ${DataFormatUtils.formatDataSize(maxMemory)}\n" +
            "已分配: ${DataFormatUtils.formatDataSize(totalMemory)}\n" +
            "已使用: ${DataFormatUtils.formatDataSize(usedMemory)}\n" +
            "可用: ${DataFormatUtils.formatDataSize(freeMemory)}\n" +
            "使用率: ${(usedMemory * 100 / maxMemory)}%"
        )
    }
    
    /**
     * 记录蓝牙状态
     */
    fun logBluetoothStatus(isConnected: Boolean, deviceInfo: String = "") {
        val timestamp = dateFormat.format(Date())
        val status = if (isConnected) "已连接" else "未连接"
        
        Timber.tag("Bluetooth").d(
            "蓝牙状态 [$timestamp]\n" +
            "状态: $status\n" +
            "设备信息: $deviceInfo"
        )
    }
    
    /**
     * 记录用户操作
     */
    fun logUserAction(action: String, details: String = "") {
        val timestamp = dateFormat.format(Date())
        Timber.tag("UserAction").i(
            "用户操作 [$timestamp]\n" +
            "动作: $action\n" +
            "详情: $details"
        )
    }
} 