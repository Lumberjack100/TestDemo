package com.shmedo.mcloudapp.device.viewmodel.state

import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class MR702HomeViewModel : CommonDeviceHomeViewModel() {
    val runningStateText = NonNullObservableField("正常")
    val isRunningStateNormal = NonNullObservableField(false)

    val isWorkModeNormal = NonNullObservableField(true)

}