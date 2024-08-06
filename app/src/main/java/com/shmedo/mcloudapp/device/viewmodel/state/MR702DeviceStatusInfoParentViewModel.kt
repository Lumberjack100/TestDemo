package com.shmedo.mcloudapp.device.viewmodel.state

import com.shmedo.lib.core.base.viewmodel.NonNullObservableField
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRBaseInfo

class MR702DeviceStatusInfoParentViewModel : BaseDeviceStatusInfoParentViewModel() {

    val wrapBaseInfo = NonNullObservableField(MRBaseInfo())
}