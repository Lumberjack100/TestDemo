package com.shmedo.mcloudapp.ui.viewmodel.state

import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class MR702HomeViewModel : CommonDeviceHomeViewModel() {
    val isWorkModeNormal = NonNullObservableField(true)
}