package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class MR702RS485Port2ViewModel : ViewModel() {
    val acquisitionFrequency = NonNullObservableField("")//采集频率
    val collectionDuration = NonNullObservableField("")//采集周期
    val collectionInterval = NonNullObservableField("")//采集间隔
    val noResponseTimes = NonNullObservableField("")//无应答次数
    val delayDuration = NonNullObservableField("")//延时时间

    val collectorType = NonNullObservableField("")//采集器类型
    val collectorAddress = NonNullObservableField("")//采集器地址
    val baudRate = NonNullObservableField("")//波特率
    val dataBit = NonNullObservableField("5")//数据位
    val checkBit = NonNullObservableField("NONE")//校验位
    val stopBit = NonNullObservableField("1")//停止位
}