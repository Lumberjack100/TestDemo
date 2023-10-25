package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class MR702RS485Port2ViewModel : ViewModel() {
    val acquisitionFrequency = NonNullObservableField("")//采集频率
    val collectionDuration = NonNullObservableField("")//采集时长
    val collectionInterval = NonNullObservableField("")//采集间隔
    val noResponseTimes = NonNullObservableField("")//无应答次数

    val collectorType = NonNullObservableField("")//采集器类型
    val collectorAddress = NonNullObservableField("")//采集器地址
    val baudRate = NonNullObservableField("")//波特率
    val dataBit = NonNullObservableField("")//数据位
    val checkBit = NonNullObservableField("")//校验位
    val stopBit = NonNullObservableField("")//停止位
}