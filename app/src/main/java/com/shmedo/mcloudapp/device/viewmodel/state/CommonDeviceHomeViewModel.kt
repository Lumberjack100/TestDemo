package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField
import com.shmedo.mcloudapp.R

open class CommonDeviceHomeViewModel : ViewModel() {
    val productLightResId = NonNullObservableField(R.drawable.ic_device_logo_def)
    val productGrayResId = NonNullObservableField(R.drawable.ic_device_logo_def)

    val productName = NonNullObservableField("")
    val deviceName = NonNullObservableField("")
    val deviceToken = NonNullObservableField("")
    val firmwareVersion = NonNullObservableField("")

    val isDeviceStateTagHighLight = NonNullObservableField(true)
    val deviceStateTagText = NonNullObservableField("在线")

    val isConnected = NonNullObservableField(false)
    val isConnectOperateVisible = NonNullObservableField(false)
    val connectOperateText = NonNullObservableField("蓝牙连接")

    val isPlatformConnectionStateVisible = NonNullObservableField(false)
    val platformConnectionStateText = NonNullObservableField("")

    val isRunningStateVisible = NonNullObservableField(false)
    val isRunningStateNormal = NonNullObservableField(false)
    val runningStateText = NonNullObservableField("")
}