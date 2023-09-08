package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField
import com.shmedo.lib.device.base.iot_cmd.model.m20.M20CurrentStateInfo

class M20CurrentStateViewModel : ViewModel() {
    val wrapStateInfo = NonNullObservableField(M20CurrentStateInfo())
}