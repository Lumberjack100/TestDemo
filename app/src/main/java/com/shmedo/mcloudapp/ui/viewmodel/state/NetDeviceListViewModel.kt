package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class NetDeviceListViewModel : ViewModel() {
    val onlineCount = NonNullObservableField(0)
    val offlineCount = NonNullObservableField(0)
    val name = NonNullObservableField("")

    val filterProductID = NonNullObservableField("")
    val filterOnlineStatus = NonNullObservableField("")
}