package com.shmedo.mcloudapp.utils

import java.text.DecimalFormat

/**
 * 数据格式化工具类
 */
object DataFormatUtils {

    private val decimalFormat = DecimalFormat("#.##")
    
    /**
     * 格式化数据大小
     * @param bytes 字节数
     * @return 格式化后的字符串
     */
    fun formatDataSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> {
                val kb = bytes / 1024.0
                "${decimalFormat.format(kb)} KB"
            }
            else -> {
                val mb = bytes / (1024.0 * 1024.0)
                "${decimalFormat.format(mb)} MB"
            }
        }
    }
    
    /**
     * 格式化传输耗时
     * @param durationMillis 耗时毫秒数
     * @return 格式化后的字符串
     */
    fun formatTransferDuration(durationMillis: Long): String {
        val seconds = durationMillis / 1000
        
        return when {
            seconds < 60 -> "${seconds} 秒"
            seconds < 3600 -> {
                val minutes = seconds / 60
                val remainingSeconds = seconds % 60
                "${minutes} 分钟 ${remainingSeconds} 秒"
            }
            else -> {
                val hours = seconds / 3600
                val remainingMinutes = (seconds % 3600) / 60
                val remainingSeconds = seconds % 60
                "${hours} 小时 ${remainingMinutes} 分钟 ${remainingSeconds} 秒"
            }
        }
    }
    
    /**
     * 格式化传输速度
     * @param bytesPerSecond 每秒字节数
     * @return 格式化后的字符串
     */
    fun formatTransferSpeed(bytesPerSecond: Long): String {
        return when {
            bytesPerSecond < 1024 -> "$bytesPerSecond B/s"
            bytesPerSecond < 1024 * 1024 -> {
                val kbps = bytesPerSecond / 1024.0
                "${decimalFormat.format(kbps)} KB/s"
            }
            else -> {
                val mbps = bytesPerSecond / (1024.0 * 1024.0)
                "${decimalFormat.format(mbps)} MB/s"
            }
        }
    }
} 