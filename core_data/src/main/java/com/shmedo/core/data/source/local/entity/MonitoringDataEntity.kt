package com.shmedo.core.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 监测数据实体
 */
@Entity(tableName = "monitoring_data")
data class MonitoringDataEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "device_sn") val deviceSn: String,
    @ColumnInfo(name = "time_str") val timeStr: String,
    @ColumnInfo(name = "sensor_data") val sensorData: String, // JSON content
    @ColumnInfo(name = "create_time") val createTime: Long = System.currentTimeMillis()
) 