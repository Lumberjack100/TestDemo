package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

open class CommonDeviceHomeViewModel : ViewModel() {
    val deviceName = NonNullObservableField("")
    val deviceToken = NonNullObservableField("")
    val productName = NonNullObservableField("")
    val firmwareVersion = NonNullObservableField("")

    val deviceStateTagText = NonNullObservableField("在线")
    val isDeviceStateTagHighLight = NonNullObservableField(true)

    val connectOperateText = NonNullObservableField("蓝牙连接")
    val isConnectOperateVisible = NonNullObservableField(false)

    val extendedField3Text = NonNullObservableField("")
    val isExtendedField3Visible = NonNullObservableField(false)

    val platformConnectionStateText = NonNullObservableField("")
    val isPlatformConnectionStateVisible = NonNullObservableField(false)

    val isPlatformOnline = NonNullObservableField(false)
}