package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.model.DeviceStatusEnum
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

open class CommonDeviceHomeViewModel : ViewModel() {
    val productErrorResId = NonNullObservableField(R.drawable.ic_device_logo_def)
    val productAlarmResId = NonNullObservableField(R.drawable.ic_device_logo_def)
    val productOfflineResId = NonNullObservableField(R.drawable.ic_device_logo_def)
    val productNormalResId = NonNullObservableField(R.drawable.ic_device_logo_def)
    val productLogoResId = NonNullObservableField(R.drawable.ic_device_logo_def)

    val productName = NonNullObservableField("")
    val productToken = NonNullObservableField("")
    val deviceToken = NonNullObservableField("")
    val firmwareVersion = NonNullObservableField("")

    val isDeviceStateTagHighLight = NonNullObservableField(true)
    val deviceStateTagText = NonNullObservableField("在线")

    val isConnected = NonNullObservableField(false)
    val isConnectOperateVisible = NonNullObservableField(false)
    val connectOperateText = NonNullObservableField("蓝牙连接")

    val isRunningStateVisible = NonNullObservableField(false) //是否显示设备运行状态
    val isRunningStateNormal = NonNullObservableField(false)
    val runningStateText = NonNullObservableField(AppContants.PLACE_HOLDER_VALUE)

    val isIOTPlatformStateVisible = NonNullObservableField(false)//是否显示米度物联网平台在线状态
    val iotPlatformStateText = NonNullObservableField("")

    val isPlatformListVisible = NonNullObservableField(false)//是否显示已连接的平台

    val deviceStatusCode = NonNullObservableField(DeviceStatusEnum.UNKNOWN.code)
    val warnErrorText = NonNullObservableField("")

    //ADME模式选择
    val isAdmeModeChooseViewVisible = NonNullObservableField(false)
    val admeModeText = NonNullObservableField("设备配置模式")
}