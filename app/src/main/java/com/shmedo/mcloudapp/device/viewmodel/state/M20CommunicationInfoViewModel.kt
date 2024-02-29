package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField
import com.shmedo.lib.device.base.iot_cmd.model.CommonCurrentStateInfo

class M20CommunicationInfoViewModel : ViewModel() {
    val wrapStateInfo = NonNullObservableField(CommonCurrentStateInfo())
}