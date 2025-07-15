package com.shmedo.core.data.repository

import com.shmedo.core.data.source.local.dao.MonitoringDataDao
import com.shmedo.core.data.source.local.entity.MonitoringDataEntity
import com.shmedo.core.model.MonitoringDataItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * 监测数据仓库 - 优化版本
 */
class MonitoringDataRepository(
    private val monitoringDataDao: MonitoringDataDao
) {
    
    companion object {
        private const val BATCH_SIZE = 1000 // 分批处理大小
        private const val MAX_CACHE_DAYS = 30 // 最大缓存天数
    }
    
    /**
     * 保存监测数据到本地 - 优化版本
     */
    suspend fun saveMonitoringData(
        deviceSn: String,
        dataList: List<MonitoringDataItem>
    ) = withContext(Dispatchers.IO) {
        try {
            // 分批处理大量数据，避免内存溢出
            dataList.chunked(BATCH_SIZE).forEach { batch ->
                val entities = batch.map { item ->
                    MonitoringDataEntity(
                        id = UUID.randomUUID().toString(),
                        deviceSn = deviceSn,
                        timeStr = item.timeStr,
                        sensorData = item.content
                    )
                }
                monitoringDataDao.batchInsert(entities)
                
                Timber.d("保存数据批次完成，设备: $deviceSn, 条数: ${entities.size}")
            }
            
            // 定期清理旧数据
            cleanupOldData(deviceSn)
            
        } catch (e: Exception) {
            Timber.e(e, "保存监测数据失败，设备: $deviceSn, 数据条数: ${dataList.size}")
            throw e
        }
    }
    
    /**
     * 获取设备的监测数据
     */
    fun getDataByDevice(deviceSn: String): Flow<List<MonitoringDataEntity>> {
        return monitoringDataDao.getDataByDevice(deviceSn)
    }
    
    /**
     * 按时间范围查询数据
     */
    suspend fun getDataByDeviceAndTimeRange(
        deviceSn: String,
        startTime: String,
        endTime: String
    ): List<MonitoringDataEntity> = withContext(Dispatchers.IO) {
        monitoringDataDao.getDataByDeviceAndTimeRange(deviceSn, startTime, endTime)
    }
    
    /**
     * 清除设备数据
     */
    suspend fun clearDeviceData(deviceSn: String) = withContext(Dispatchers.IO) {
        monitoringDataDao.deleteByDevice(deviceSn)
    }
    
    /**
     * 获取数据总数
     */
    suspend fun getDataCount(deviceSn: String): Int = withContext(Dispatchers.IO) {
        monitoringDataDao.getCountByDevice(deviceSn)
    }
    
    /**
     * 清理旧数据
     */
    private suspend fun cleanupOldData(deviceSn: String) {
        try {
            val cutoffDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(Date(System.currentTimeMillis() - MAX_CACHE_DAYS * 24 * 60 * 60 * 1000L))
            
            val deletedCount = monitoringDataDao.deleteOldData(deviceSn, cutoffDate)
            if (deletedCount > 0) {
                Timber.d("清理旧数据完成，设备: $deviceSn, 删除条数: $deletedCount")
            }
        } catch (e: Exception) {
            Timber.w(e, "清理旧数据失败，设备: $deviceSn")
        }
    }
    
    /**
     * 检查数据库大小并进行清理
     */
    suspend fun checkAndCleanDatabase(deviceSn: String) = withContext(Dispatchers.IO) {
        try {
            val totalCount = monitoringDataDao.getCountByDevice(deviceSn)
            
            // 如果数据量过大，清理一部分旧数据
            if (totalCount > 50000) {
                val cleanupDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L)) // 保留7天数据
                
                val deletedCount = monitoringDataDao.deleteOldData(deviceSn, cleanupDate)
                Timber.i("数据库清理完成，设备: $deviceSn, 删除条数: $deletedCount")
            }
        } catch (e: Exception) {
            Timber.e(e, "数据库清理失败，设备: $deviceSn")
        }
    }
} 