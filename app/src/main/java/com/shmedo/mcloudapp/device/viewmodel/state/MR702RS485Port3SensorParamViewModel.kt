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
    val baudRate = NonNullObservableField("")
    val dataBit = NonNullObservableField("")
    val checkBit = NonNullObservableField("")
    val stopBit = NonNullObservableField("")

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