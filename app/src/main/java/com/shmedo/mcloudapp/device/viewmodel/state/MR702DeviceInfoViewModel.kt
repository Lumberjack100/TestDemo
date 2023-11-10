package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRBaseInfo

class MR702DeviceInfoViewModel : ViewModel() {
    val productName = NonNullObservableField("")
    val productType = NonNullObservableField("")

    val wrapBaseInfo = NonNullObservableField(MRBaseInfo())
}