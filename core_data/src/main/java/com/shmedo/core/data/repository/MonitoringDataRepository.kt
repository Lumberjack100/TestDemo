package com.shmedo.core.data.repository

import com.shmedo.core.data.source.local.dao.MonitoringDataDao
import com.shmedo.core.data.source.local.entity.MonitoringDataEntity
import com.shmedo.core.model.MonitoringDataItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * 监测数据仓库
 */
class MonitoringDataRepository(
    private val monitoringDataDao: MonitoringDataDao
) {
    /**
     * 保存监测数据到本地
     */
    suspend fun saveMonitoringData(
        deviceSn: String,
        dataList: List<MonitoringDataItem>
    ) = withContext(Dispatchers.IO) {
        val entities = dataList.map { item ->
            MonitoringDataEntity(
                id = UUID.randomUUID().toString(),
                deviceSn = deviceSn,
                timeStr = item.timeStr,
                sensorData = item.content
            )
        }
        monitoringDataDao.insertAll(entities)
    }
    
    /**
     * 获取设备的监测数据
     */
    fun getMonitoringData(deviceSn: String): Flow<List<MonitoringDataEntity>> {
        return monitoringDataDao.getDataByDevice(deviceSn)
    }
    
    /**
     * 按时间范围查询数据
     */
    suspend fun getDataByTimeRange(
        deviceSn: String,
        startTime: String,
        endTime: String
    ): List<MonitoringDataEntity> = withContext(Dispatchers.IO) {
        monitoringDataDao.getDataByTimeRange(deviceSn, startTime, endTime)
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
} 