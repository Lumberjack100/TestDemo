package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class MR702RS485Port3SensorParamViewModel : ViewModel() {
    val isEditable = NonNullObservableField(false)

    val status = NonNullObservableField("已接入")
    val isOpened = NonNullObservableField(true)
    val sensorType = NonNullObservableField(1)
    val sensorName = NonNullObservableField("")
    val address = NonNullObservableField("")
    val baudRate = NonNullObservableField("9600")
    val dataBit = NonNullObservableField("5")//数据位
    val checkBit = NonNullObservableField("NONE")//校验位
    val stopBit = NonNullObservableField("1")//停止位

    val solarVoltage = NonNullObservableField("")
    val batteryVoltage = NonNullObservableField("")
    val solarPower = NonNullObservableField("")
    val loadPower = NonNullObservableField("")

    val duration = NonNullObservableField("")
    val interval = NonNullObservableField("")
    val volume = NonNullObservableField(20)

    val ledType = NonNullObservableField("")
    val screenTime = NonNullObservableField("")
}