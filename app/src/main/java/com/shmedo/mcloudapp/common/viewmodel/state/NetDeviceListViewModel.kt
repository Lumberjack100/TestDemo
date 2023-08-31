package com.shmedo.mcloudapp.common.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class NetDeviceListViewModel : ViewModel() {
    @JvmField
    val onlineCount = NonNullObservableField(0)

    @JvmField
    val offlineCount = NonNullObservableField(0)

    @JvmField
    val name = NonNullObservableField("")
}