package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField
import com.shmedo.lib.device.base.iot_cmd.model.das.DasBaseInfo

class DasBaseInfoViewModel : ViewModel() {
    val wrapBaseInfo = NonNullObservableField(DasBaseInfo())
    val signal = NonNullObservableField("")
    val signalValue = NonNullObservableField(0)
}