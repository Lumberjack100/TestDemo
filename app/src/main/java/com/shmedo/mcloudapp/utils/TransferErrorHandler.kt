package com.shmedo.mcloudapp.utils

import kotlinx.coroutines.delay
import timber.log.Timber
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * 传输错误处理工具类
 */
object TransferErrorHandler {
    
    /**
     * 传输错误类型
     */
    sealed class TransferError(val message: String, val isRetryable: Boolean = false) {
        object NetworkError : TransferError("网络连接异常", true)
        object BleDisconnected : TransferError("蓝牙连接已断开", true)
        object DeviceNotSupported : TransferError("设备不支持此功能", false)
        object StorageSpaceInsufficient : TransferError("存储空间不足", false)
        object TimeoutError : TransferError("操作超时", true)
        object UnknownDeviceError : TransferError("未知设备错误", true)
        data class CommandFailed(val reason: String) : TransferError("指令执行失败：$reason", true)
        data class ParseError(val data: String) : TransferError("数据解析失败", false)
        data class DatabaseError(val cause: String) : TransferError("数据库操作失败：$cause", true)
        data class UnknownError(val cause: String) : TransferError("未知错误：$cause", true)
    }
    
    /**
     * 重试配置
     */
    data class RetryConfig(
        val maxRetries: Int = 3,
        val initialDelay: Long = 1000L,
        val maxDelay: Long = 10000L,
        val multiplier: Double = 2.0
    )
    
    /**
     * 将异常转换为传输错误
     */
    fun mapExceptionToTransferError(exception: Throwable): TransferError {
        return when (exception) {
            is SocketTimeoutException -> TransferError.TimeoutError
            is ConnectException -> TransferError.NetworkError
            is UnknownHostException -> TransferError.NetworkError
            is IllegalArgumentException -> TransferError.ParseError(exception.message ?: "")
            is RuntimeException -> {
                if (exception.message?.contains("BLE") == true) {
                    TransferError.BleDisconnected
                } else {
                    TransferError.UnknownError(exception.message ?: "")
                }
            }
            else -> TransferError.UnknownError(exception.message ?: "")
        }
    }
    
    /**
     * 执行带重试的操作
     */
    suspend fun <T> executeWithRetry(
        config: RetryConfig = RetryConfig(),
        operation: suspend () -> T
    ): T {
        var lastException: Throwable? = null
        var delay = config.initialDelay
        
        repeat(config.maxRetries + 1) { attempt ->
            try {
                return operation()
            } catch (e: Exception) {
                lastException = e
                val transferError = mapExceptionToTransferError(e)
                
                Timber.w("传输操作失败 (尝试 ${attempt + 1}/${config.maxRetries + 1}): ${transferError.message}")
                
                // 如果是最后一次尝试或者错误不可重试，则抛出异常
                if (attempt == config.maxRetries || !transferError.isRetryable) {
                    throw e
                }
                
                // 等待后重试
                val actualDelay = minOf(delay, config.maxDelay)
                Timber.d("等待 ${actualDelay}ms 后重试...")
                delay(actualDelay)
                delay = (delay * config.multiplier).toLong()
            }
        }
        
        // 这里不应该到达，但为了编译通过
        throw lastException ?: RuntimeException("未知错误")
    }
    
    /**
     * 获取用户友好的错误消息
     */
    fun getUserFriendlyErrorMessage(error: TransferError): String {
        return when (error) {
            is TransferError.NetworkError -> "网络连接异常，请检查网络连接后重试"
            is TransferError.BleDisconnected -> "蓝牙连接已断开，请检查蓝牙连接后重试"
            is TransferError.DeviceNotSupported -> "当前设备不支持此功能"
            is TransferError.StorageSpaceInsufficient -> "手机存储空间不足，请清理空间后重试"
            is TransferError.TimeoutError -> "操作超时，请检查设备连接后重试"
            is TransferError.UnknownDeviceError -> "设备响应异常，请重新连接设备后重试"
            is TransferError.CommandFailed -> "设备指令执行失败：${error.reason}"
            is TransferError.ParseError -> "数据格式异常，请联系技术支持"
            is TransferError.DatabaseError -> "数据保存失败，请检查存储权限"
            is TransferError.UnknownError -> "操作失败，请重试或联系技术支持"
        }
    }
    
    /**
     * 检查错误是否可以重试
     */
    fun shouldRetry(error: TransferError): Boolean {
        return error.isRetryable
    }
} 