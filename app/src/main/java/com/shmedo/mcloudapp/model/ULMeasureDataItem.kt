package com.shmedo.mcloudapp.model

import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

/**
 * 创建者：gonghe
 * 创建时间：2025/1/24
 * 描述：北斗林木生长监测终端设备测量数据项
 */
data class ULMeasureDataItem(
    val lFInitial: NonNullObservableField<String> = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE), // 初始测量值
    val lFCurrent: NonNullObservableField<String> = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE), // 实时测量值
    val lFCumulative: NonNullObservableField<String> = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE), // 累计变化量
    val xAngle: NonNullObservableField<String> = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE), // x 轴角度
    val yAngle: NonNullObservableField<String> = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE), // y 轴角度
    val zAngle: NonNullObservableField<String> = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE) // z 轴角度
) {

    /**
     * 刷新测量数据状态
     */
    fun refreshStatus(
        newLFInitial: String,
        newLFCurrent: String,
        newLFCumulative: String,
        newXAngle: String,
        newYAngle: String,
        newZAngle: String
    ) {
        lFInitial.set(newLFInitial)
        lFCurrent.set(newLFCurrent)
        lFCumulative.set(newLFCumulative)
        xAngle.set(newXAngle)
        yAngle.set(newYAngle)
        zAngle.set(newZAngle)
    }

    fun refreshDisplacementStatus(
        newLFInitial: String,
        newLFCurrent: String,
        newLFCumulative: String,
    ) {
        lFInitial.set(newLFInitial)
        lFCurrent.set(newLFCurrent)
        lFCumulative.set(newLFCumulative)
    }

    fun refreshAngleStatus(
        newXAngle: String,
        newYAngle: String,
        newZAngle: String
    ) {
        xAngle.set(newXAngle)
        yAngle.set(newYAngle)
        zAngle.set(newZAngle)
    }

} 