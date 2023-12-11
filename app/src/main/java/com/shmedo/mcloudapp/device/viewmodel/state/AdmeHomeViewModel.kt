package com.shmedo.mcloudapp.device.viewmodel.state

import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class AdmeHomeViewModel : CommonDeviceHomeViewModel() {
    val mode = NonNullObservableField("设备配置模式")

}