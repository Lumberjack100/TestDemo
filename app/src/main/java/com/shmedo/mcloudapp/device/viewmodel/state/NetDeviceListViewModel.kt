package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class NetDeviceListViewModel : ViewModel() {
    val onlineCount = NonNullObservableField(0)
    val offlineCount = NonNullObservableField(0)
    val name = NonNullObservableField("")
}