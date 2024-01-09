package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class AdmeCurrentStateViewModel : ViewModel() {
    val ctrMotionInfoVisible = NonNullObservableField(false)
    val measureMode = NonNullObservableField("")
    val motorInfo = NonNullObservableField("")
    val measurePoint = NonNullObservableField("")

    val sn = NonNullObservableField("")
    val productType = NonNullObservableField("")
    val sim = NonNullObservableField("")
    val imei = NonNullObservableField("")
    val firmwareVersion = NonNullObservableField("")
    val signal = NonNullObservableField("")
    val signalValue = NonNullObservableField(0)

    val deviceNormal = NonNullObservableField(false)
    val deviceAbnormalDiagnosis = NonNullObservableField("")
    val workMode = NonNullObservableField("")
    val ctrInputVoltage = NonNullObservableField("")
    val driverInputVoltage = NonNullObservableField("")
    val deviceTemperature = NonNullObservableField("")
    val deviceHumidity = NonNullObservableField("")
    val deviceDropNumber = NonNullObservableField("")
    val deviceMileage = NonNullObservableField("")
    val nextMeasureTime = NonNullObservableField("")

    val inclinometerType = NonNullObservableField("")
    val inclinometerChannelNumber = NonNullObservableField("")
    val inclinometerLocationInfo = NonNullObservableField("")
    val inclinometerVoltage = NonNullObservableField("")
    val inclinometerTemperature = NonNullObservableField("")
    val inclinometer4gSignal = NonNullObservableField("")
    val inclinometer4gSignalValue = NonNullObservableField(0)
    val inclinometerBluetoothSignal = NonNullObservableField("")
    val inclinometerBluetoothSignalValue = NonNullObservableField(0)
}