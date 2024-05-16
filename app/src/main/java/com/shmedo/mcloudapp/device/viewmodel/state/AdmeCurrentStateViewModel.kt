package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeCurrentStateInfo

class AdmeCurrentStateViewModel : ViewModel() {
    val wrapStateInfo = NonNullObservableField(AdmeCurrentStateInfo())

    val ctrMotionInfoVisible = NonNullObservableField(false)
    val isMotorInfoNormal = NonNullObservableField(true)
    val measureMode = NonNullObservableField("")
    val motorInfo = NonNullObservableField("")
    val measurePoint = NonNullObservableField("")


    val deviceNormal = NonNullObservableField(false)
    val workMode = NonNullObservableField("")

    val signalValue = NonNullObservableField(0)
    val inclinometerBluetoothSignalValue = NonNullObservableField(0)
}