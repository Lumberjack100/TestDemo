package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

open class CommonDeviceHomeViewModel : ViewModel() {
    val productName = NonNullObservableField("")
    val deviceName = NonNullObservableField("")
    val deviceToken = NonNullObservableField("")
    val firmwareVersion = NonNullObservableField("")

    val isDeviceStateTagHighLight = NonNullObservableField(true)
    val deviceStateTagText = NonNullObservableField("在线")
    val isConnectOperateVisible = NonNullObservableField(false)
    val connectOperateText = NonNullObservableField("蓝牙连接")
    val isPlatformConnectionStateVisible = NonNullObservableField(false)
    val platformConnectionStateText = NonNullObservableField("")
}