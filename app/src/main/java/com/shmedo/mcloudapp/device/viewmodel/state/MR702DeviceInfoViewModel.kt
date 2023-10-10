package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRBaseInfo
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRModuleStatusInfo

class MR702DeviceInfoViewModel : ViewModel() {
    val productName = NonNullObservableField("终端遥控测试机")
    val productType = NonNullObservableField("型号：MR702-SL")

    val wrapBaseInfo = NonNullObservableField(MRBaseInfo())

    val wrapModuleStatusInfo = NonNullObservableField(MRModuleStatusInfo())
}