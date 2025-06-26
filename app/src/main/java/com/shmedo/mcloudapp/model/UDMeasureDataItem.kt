package com.shmedo.mcloudapp.model

import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

/**
 * 创建者：gonghe
 * 创建时间：2025/1/24
 * 描述：UD 雷达水位计测量数据项
 */
data class UDMeasureDataItem(
    val waterSurfaceElevation: NonNullObservableField<String> = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE), // 水面高程
    val airDistance: NonNullObservableField<String> = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE), // 空高距离
    val installationAngle: NonNullObservableField<String> = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE), // 安装角度
    val todayRainfall: NonNullObservableField<String> = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE), // 今日雨量
    val measurementTime: NonNullObservableField<String> = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE), // 测量时间
    val isMeasuring: NonNullObservableField<Boolean> = NonNullObservableField(false) // 是否正在测量
) {
    /**
     * 刷新测量数据状态
     */
    fun refreshMeasureData(
        newWaterSurfaceElevation: String,
        newAirDistance: String,
        newInstallationAngle: String,
        newTodayRainfall: String,
        newMeasurementTime: String
    ) {
        waterSurfaceElevation.set(newWaterSurfaceElevation)
        airDistance.set(newAirDistance)
        installationAngle.set(newInstallationAngle)
        todayRainfall.set(newTodayRainfall)
        measurementTime.set(newMeasurementTime)
    }

    /**
     * 设置测量状态
     */
    fun setMeasuringStatus(measuring: Boolean) {
        isMeasuring.set(measuring)
    }
}