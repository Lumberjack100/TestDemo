package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class AdmeCurrentStateViewModel : ViewModel() {
    val ctrMotionInfoVisible = NonNullObservableField(false)
    val isMotorInfoNormal = NonNullObservableField(true)
    val measureMode = NonNullObservableField("")
    val motorInfo = NonNullObservableField("")
    val measurePoint = NonNullObservableField("")

    val signalValue = NonNullObservableField(0)
}