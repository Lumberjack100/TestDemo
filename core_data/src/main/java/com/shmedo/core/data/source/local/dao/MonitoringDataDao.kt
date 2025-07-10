package com.shmedo.core.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shmedo.core.data.source.local.entity.MonitoringDataEntity
import kotlinx.coroutines.flow.Flow

/**
 * 监测数据DAO
 */
@Dao
interface MonitoringDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: MonitoringDataEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun batchInsert(dataList: List<MonitoringDataEntity>)
    
    @Query("SELECT * FROM monitoring_data WHERE device_sn = :deviceSn ORDER BY time_str DESC")
    fun getDataByDevice(deviceSn: String): Flow<List<MonitoringDataEntity>>
    
    @Query("SELECT * FROM monitoring_data WHERE device_sn = :deviceSn AND time_str BETWEEN :startTime AND :endTime ORDER BY time_str DESC")
    suspend fun getDataByDeviceAndTimeRange(deviceSn: String, startTime: String, endTime: String): List<MonitoringDataEntity>
    
    @Query("DELETE FROM monitoring_data WHERE device_sn = :deviceSn")
    suspend fun deleteByDevice(deviceSn: String)
    
    @Query("SELECT COUNT(*) FROM monitoring_data WHERE device_sn = :deviceSn")
    suspend fun getCountByDevice(deviceSn: String): Int
    
    @Query("DELETE FROM monitoring_data WHERE device_sn = :deviceSn AND time_str < :cutoffDate")
    suspend fun deleteOldData(deviceSn: String, cutoffDate: String): Int
} 