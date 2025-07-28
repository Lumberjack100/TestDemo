package com.shmedo.mcloudapp.model

import androidx.databinding.ObservableField

/**
 * 创建者：gonghe
 * 创建时间：2025/1/24
 * 描述：北斗数传配置项数据模型
 */
data class BeidouDataTransmissionItem(
    val description: String = "通过外接的北斗数传终端，传输采集的传感器数据",
    val reportInterval: ObservableField<String> = ObservableField("")
) {
    fun getReportIntervalStr(): String {
        return reportInterval.get().toString()
    }

    fun setReportInterval(value: String) {
        reportInterval.set(value)
    }
}