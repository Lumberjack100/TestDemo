package com.shmedo.mcloudapp.model

import androidx.databinding.ObservableField

/**
 * 创建者：gonghe
 * 创建时间：2025/1/24
 * 描述：上报周期配置项数据模型
 */
data class DataReportingPeriodItem(
    val reportMethod: ObservableField<String> = ObservableField("定时定点上报"),
    val reportStartTimeHour: ObservableField<String> = ObservableField("08:00"),
    val reportStartTimeMinute: ObservableField<String> = ObservableField("15"),
    val reportInterval: ObservableField<String> = ObservableField("120")
) {

    fun getReportMethodStr(): String {
        return reportMethod.get().toString()
    }

    fun setReportMethod(value: String) {
        reportMethod.set(value)
    }

    fun getReportStartTimeHourStr(): String {
        return reportStartTimeHour.get().toString()
    }
    fun setReportStartTimeHour(value: String) {
        reportStartTimeHour.set(value)
    }

    fun getReportStartTimeMinuteStr(): String {
        return reportStartTimeMinute.get().toString()
    }
    fun setReportStartTimeMinute(value: String) {
        reportStartTimeMinute.set(value)
    }


    fun getReportIntervalStr(): String {
        return reportInterval.get().toString()
    }

    fun setReportInterval(value: String) {
        reportInterval.set(value)
    }
}