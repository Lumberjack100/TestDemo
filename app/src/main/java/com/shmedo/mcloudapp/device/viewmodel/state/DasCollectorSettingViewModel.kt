package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField
import com.shmedo.lib.device.base.iot_cmd.model.das.DasCollectorInfo

class DasCollectorSettingViewModel : ViewModel() {
    val infoWrapper = NonNullObservableField<DasCollectorInfo>(DasCollectorInfo())

    val isEditable = NonNullObservableField(true)
    val isShowSensitivity = NonNullObservableField(false)
    val collectorAddress = NonNullObservableField("")//采集器地址
    val solvingInterval = NonNullObservableField("")//采集器解算间隔
    val standbyTime = NonNullObservableField("")//待机时间
    val collectionInterval = NonNullObservableField("")//采集器采集间隔
    val sensitivity = NonNullObservableField("")//灵敏度
}