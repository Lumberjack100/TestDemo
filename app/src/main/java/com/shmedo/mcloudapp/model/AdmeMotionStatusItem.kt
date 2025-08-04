package com.shmedo.mcloudapp.model

import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

/**
 * @author：gonghe
 * @time: 2025/1/25
 * @desc: ADME CTR 运动状态数据项
 */
data class AdmeMotionStatusItem(
    val ctrMotionInfoVisible: NonNullObservableField<Boolean> = NonNullObservableField(false),
    val isMotorInfoNormal: NonNullObservableField<Boolean> = NonNullObservableField(true),
    val measureMode: NonNullObservableField<String> = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE),
    val motorInfo: NonNullObservableField<String> = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE),
    val measurePoint: NonNullObservableField<String> = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE)
) {

    /**
     * 刷新运动状态信息
     */
    fun refreshMotionStatus(
        visible: Boolean,
        isNormal: Boolean,
        mode: String,
        info: String,
        point: String = AppContants.PLACE_HOLDER_VALUE
    ) {
        ctrMotionInfoVisible.set(visible)
        isMotorInfoNormal.set(isNormal)
        measureMode.set(mode)
        motorInfo.set(info)
        measurePoint.set(point)
    }

    /**
     * 隐藏运动状态信息
     */
    fun hideMotionStatus() {
        ctrMotionInfoVisible.set(false)
    }

    /**
     * 显示运动状态信息
     */
    fun showMotionStatus() {
        ctrMotionInfoVisible.set(true)
    }
}