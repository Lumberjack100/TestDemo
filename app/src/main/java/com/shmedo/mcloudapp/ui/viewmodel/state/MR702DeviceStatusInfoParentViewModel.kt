package com.shmedo.mcloudapp.ui.viewmodel.state

import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRBaseInfo

class MR702DeviceStatusInfoParentViewModel : BaseDeviceStatusInfoParentViewModel() {

    val wrapBaseInfo = NonNullObservableField(MRBaseInfo())
}