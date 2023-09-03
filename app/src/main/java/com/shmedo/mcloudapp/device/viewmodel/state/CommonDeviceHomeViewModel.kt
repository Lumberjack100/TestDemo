package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

open class CommonDeviceHomeViewModel : ViewModel() {
    val deviceName = NonNullObservableField("")
    val deviceToken = NonNullObservableField("")
    val productName = NonNullObservableField("")
    val firmwareVersion = NonNullObservableField("")
    val onlineStatus = NonNullObservableField("在线")
    val isOnline = NonNullObservableField(false)
    val extendedField3 = NonNullObservableField("")
    val platformConnectionState = NonNullObservableField("")
    val connectOperate = NonNullObservableField("")
    val isExtendedField3Visible = NonNullObservableField(false)
    val isPlatformConnectionStateVisible = NonNullObservableField(false)
    val isConnectOperateVisible = NonNullObservableField(false)

}