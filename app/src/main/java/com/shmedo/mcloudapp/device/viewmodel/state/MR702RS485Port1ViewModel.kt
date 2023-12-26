package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class MR702RS485Port1ViewModel : ViewModel() {
    val acquisitionFrequency = NonNullObservableField("")//采集频率
    val collectionDuration = NonNullObservableField("")//采集周期
    val collectionTimes = NonNullObservableField("")//采集次数
    val noResponseTimes = NonNullObservableField("")//无应答次数
    val delayDuration = NonNullObservableField("")//延时时间
}