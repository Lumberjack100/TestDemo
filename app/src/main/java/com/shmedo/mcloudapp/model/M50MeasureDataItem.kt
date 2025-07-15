package com.shmedo.mcloudapp.model

import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

/**
 * 创建者：gonghe
 * 创建时间：2025/1/24
 * 描述：M50 测量数据项
 */
data class M50MeasureDataItem(
    val xDisplacement: NonNullObservableField<String> = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE), // 东向位移量
    val yDisplacement: NonNullObservableField<String> = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE), // 北向位移量
    val zDisplacement: NonNullObservableField<String> = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE)  // 垂直位移量
) {
    /**
     * 刷新测量数据状态
     */
    fun refreshStatus(
        newXDisplacement: String,
        newYDisplacement: String,
        newZDisplacement: String
    ) {
        xDisplacement.set(newXDisplacement)
        yDisplacement.set(newYDisplacement)
        zDisplacement.set(newZDisplacement)
    }
} 