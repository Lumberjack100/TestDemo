package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonCurrentStateInfo2

class GW100BaseInfoViewModel : ViewModel() {
    val wrapStateInfo = NonNullObservableField(CommonCurrentStateInfo2())

    val deviceNormal = NonNullObservableField(true)
}