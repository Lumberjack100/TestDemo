package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

open class CommonDeviceHomeViewModel : ViewModel() {
    val deviceName = NonNullObservableField("")
    val deviceToken = NonNullObservableField("")
    val productName = NonNullObservableField("")
    val firmwareVersion = NonNullObservableField("")

    val deviceState = NonNullObservableField("在线")
    val isDeviceStateHighLight = NonNullObservableField(true)

    val connectOperate = NonNullObservableField("蓝牙连接")
    val isConnectOperateVisible = NonNullObservableField(false)

    val extendedField3 = NonNullObservableField("")
    val isExtendedField3Visible = NonNullObservableField(false)

    val platformConnectionState = NonNullObservableField("")
    val isPlatformConnectionStateVisible = NonNullObservableField(false)

    val isPlatformOnline = NonNullObservableField(false)
    val isBleConnected = NonNullObservableField(false)
}