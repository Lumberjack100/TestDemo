package com.shmedo.mcloudapp.utils

import android.os.Debug
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * 性能监控工具类
 */
class PerformanceMonitor {
    
    // 传输性能指标
    private val transferMetrics = ConcurrentHashMap<String, TransferMetrics>()
    private val _performanceState = MutableStateFlow(PerformanceState())
    val performanceState: StateFlow<PerformanceState> = _performanceState.asStateFlow()
    
    // 内存使用监控
    private val memoryUsage = AtomicLong(0)
    private val maxMemoryUsage = AtomicLong(0)
    
    /**
     * 传输性能指标
     */
    data class TransferMetrics(
        val deviceSn: String,
        val startTime: Long,
        val endTime: Long = 0,
        val totalBytes: Long = 0,
        val totalCount: Int = 0,
        val errorCount: Int = 0,
        val retryCount: Int = 0
    ) {
        val duration: Long get() = if (endTime > 0) endTime - startTime else System.currentTimeMillis() - startTime
        val averageSpeed: Long get() = if (duration > 0) totalBytes * 1000 / duration else 0
        val successRate: Double get() = if (totalCount > 0) (totalCount - errorCount).toDouble() / totalCount else 0.0
    }
    
    /**
     * 性能状态
     */
    data class PerformanceState(
        val isMonitoring: Boolean = false,
        val currentTransferSpeed: Long = 0,
        val averageTransferSpeed: Long = 0,
        val memoryUsage: Long = 0,
        val maxMemoryUsage: Long = 0,
        val totalTransferred: Long = 0,
        val errorRate: Double = 0.0
    )
    
    /**
     * 开始监控传输性能
     */
    fun startMonitoring(deviceSn: String) {
        val metrics = TransferMetrics(
            deviceSn = deviceSn,
            startTime = System.currentTimeMillis()
        )
        transferMetrics[deviceSn] = metrics
        
        updatePerformanceState(isMonitoring = true)
        
        Timber.tag("PerformanceMonitor").i("开始监控传输性能 - 设备: $deviceSn")
    }
    
    /**
     * 停止监控传输性能
     */
    fun stopMonitoring(deviceSn: String) {
        transferMetrics[deviceSn]?.let { metrics ->
            val updatedMetrics = metrics.copy(endTime = System.currentTimeMillis())
            transferMetrics[deviceSn] = updatedMetrics
            
            logTransferSummary(updatedMetrics)
            updatePerformanceState(isMonitoring = false)
        }
    }
    
    /**
     * 记录传输数据
     */
    fun recordTransferData(deviceSn: String, bytes: Long, count: Int) {
        transferMetrics[deviceSn]?.let { metrics ->
            val updatedMetrics = metrics.copy(
                totalBytes = metrics.totalBytes + bytes,
                totalCount = metrics.totalCount + count
            )
            transferMetrics[deviceSn] = updatedMetrics
            
            // 更新当前传输速度
            val currentSpeed = if (metrics.duration > 0) {
                updatedMetrics.totalBytes * 1000 / metrics.duration
            } else 0
            
            updatePerformanceState(
                currentTransferSpeed = currentSpeed,
                averageTransferSpeed = updatedMetrics.averageSpeed,
                totalTransferred = updatedMetrics.totalBytes
            )
        }
    }
    
    /**
     * 记录传输错误
     */
    fun recordTransferError(deviceSn: String) {
        transferMetrics[deviceSn]?.let { metrics ->
            val updatedMetrics = metrics.copy(errorCount = metrics.errorCount + 1)
            transferMetrics[deviceSn] = updatedMetrics
            
            updatePerformanceState(errorRate = 1.0 - updatedMetrics.successRate)
        }
    }
    
    /**
     * 记录重试次数
     */
    fun recordRetry(deviceSn: String) {
        transferMetrics[deviceSn]?.let { metrics ->
            val updatedMetrics = metrics.copy(retryCount = metrics.retryCount + 1)
            transferMetrics[deviceSn] = updatedMetrics
        }
    }
    
    /**
     * 监控内存使用
     */
    fun monitorMemoryUsage() {
        val currentMemory = Debug.getNativeHeapAllocatedSize()
        memoryUsage.set(currentMemory)
        
        val currentMax = maxMemoryUsage.get()
        if (currentMemory > currentMax) {
            maxMemoryUsage.set(currentMemory)
        }
        
        updatePerformanceState(
            memoryUsage = currentMemory,
            maxMemoryUsage = maxMemoryUsage.get()
        )
        
        // 内存使用过高时记录警告
        val maxMemory = Runtime.getRuntime().maxMemory()
        if (currentMemory > maxMemory * 0.8) {
            Timber.tag("PerformanceMonitor").w(
                "内存使用过高: ${DataFormatUtils.formatDataSize(currentMemory)} / ${DataFormatUtils.formatDataSize(maxMemory)}"
            )
        }
    }
    
    /**
     * 测量操作执行时间
     */
    suspend fun <T> measureExecutionTime(
        operation: String,
        block: suspend () -> T
    ): T {
        val startTime = System.currentTimeMillis()
        return try {
            val result = block()
            val duration = System.currentTimeMillis() - startTime
            Timber.tag("PerformanceMonitor").d("$operation 耗时: ${duration}ms")
            result
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            Timber.tag("PerformanceMonitor").w("$operation 失败，耗时: ${duration}ms - ${e.message}")
            throw e
        }
    }
    
    /**
     * 获取传输性能指标
     */
    fun getTransferMetrics(deviceSn: String): TransferMetrics? {
        return transferMetrics[deviceSn]
    }
    
    /**
     * 清理监控数据
     */
    fun clearMetrics(deviceSn: String) {
        transferMetrics.remove(deviceSn)
        Timber.tag("PerformanceMonitor").i("清理设备监控数据: $deviceSn")
    }
    
    /**
     * 获取所有监控数据
     */
    fun getAllMetrics(): Map<String, TransferMetrics> {
        return transferMetrics.toMap()
    }
    
    /**
     * 更新性能状态
     */
    private fun updatePerformanceState(
        isMonitoring: Boolean? = null,
        currentTransferSpeed: Long? = null,
        averageTransferSpeed: Long? = null,
        memoryUsage: Long? = null,
        maxMemoryUsage: Long? = null,
        totalTransferred: Long? = null,
        errorRate: Double? = null
    ) {
        val currentState = _performanceState.value
        _performanceState.value = currentState.copy(
            isMonitoring = isMonitoring ?: currentState.isMonitoring,
            currentTransferSpeed = currentTransferSpeed ?: currentState.currentTransferSpeed,
            averageTransferSpeed = averageTransferSpeed ?: currentState.averageTransferSpeed,
            memoryUsage = memoryUsage ?: currentState.memoryUsage,
            maxMemoryUsage = maxMemoryUsage ?: currentState.maxMemoryUsage,
            totalTransferred = totalTransferred ?: currentState.totalTransferred,
            errorRate = errorRate ?: currentState.errorRate
        )
    }
    
    /**
     * 记录传输总结
     */
    private fun logTransferSummary(metrics: TransferMetrics) {
        Timber.tag("PerformanceMonitor").i(
            "传输完成总结 - 设备: ${metrics.deviceSn}, " +
            "耗时: ${DataFormatUtils.formatTransferDuration(metrics.duration)}, " +
            "总大小: ${DataFormatUtils.formatDataSize(metrics.totalBytes)}, " +
            "总条数: ${metrics.totalCount}, " +
            "平均速度: ${DataFormatUtils.formatTransferSpeed(metrics.averageSpeed)}, " +
            "成功率: ${(metrics.successRate * 100).toInt()}%, " +
            "错误次数: ${metrics.errorCount}, " +
            "重试次数: ${metrics.retryCount}"
        )
    }
} 