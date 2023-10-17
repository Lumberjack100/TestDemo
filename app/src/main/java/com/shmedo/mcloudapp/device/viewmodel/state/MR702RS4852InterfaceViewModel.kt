package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class MR702RS4852InterfaceViewModel : ViewModel() {
    val interfaceName = NonNullObservableField("")
    val interfaceDesc = NonNullObservableField("最多支持32支传感器接入")

    val acquisitionFrequency = NonNullObservableField("")//采集频率
    val collectionDuration = NonNullObservableField("")//采集时长
    val collectionInterval = NonNullObservableField("")//采集间隔
    val noResponseTimes = NonNullObservableField("")//无应答次数
}