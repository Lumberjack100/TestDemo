package com.shmedo.mcloudapp.device.viewmodel.state

import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/6
 *
 * 描述： TODO
 *
 *
 */
class LR200ZeroValueCalibrationViewModel : CommandResponseViewModel() {
    val zeroValue = NonNullObservableField("")//零位预设值
    val zeroValueMeasured = NonNullObservableField("")//零位测量值
    //
}