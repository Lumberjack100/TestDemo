package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField
import com.shmedo.mcloudapp.R

open class CommonDeviceHomeViewModel : ViewModel() {
    val productLightResId = NonNullObservableField(R.drawable.ic_device_logo_def)
    val productGrayResId = NonNullObservableField(R.drawable.ic_device_logo_def)
    val productLogoResId = NonNullObservableField(R.drawable.ic_device_logo_def)

    val productName = NonNullObservableField("")
    val deviceName = NonNullObservableField("")
    val deviceToken = NonNullObservableField("")
    val firmwareVersion = NonNullObservableField("--")

    val isDeviceStateTagHighLight = NonNullObservableField(true)
    val deviceStateTagText = NonNullObservableField("在线")

    val isConnected = NonNullObservableField(false)
    val isConnectOperateVisible = NonNullObservableField(false)
    val connectOperateText = NonNullObservableField("蓝牙连接")

    //运行状态
    val isRunningStateVisible = NonNullObservableField(false)
    val isRunningStateNormal = NonNullObservableField(false)
    val runningStateText = NonNullObservableField("--")

    //米度物联网平台在线状态
    val isIOTPlatformStateVisible = NonNullObservableField(false)
    val iotPlatformStateText = NonNullObservableField("")

    //已连接的平台
    val isPlatformsVisible = NonNullObservableField(false)

    //ADME模式选择
    val isAdmeModeChooseViewVisible = NonNullObservableField(false)
    val admeModeText = NonNullObservableField("设备配置模式")
}