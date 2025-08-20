package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class DeviceReplacementViewModel : ViewModel() {
    val oldDeviceToken = NonNullObservableField("")//旧设备Token
    val newDeviceToken = NonNullObservableField("")//新设备Token
    val oldFirmwareVersion = NonNullObservableField("") //旧固件版本
    val newFirmwareVersion = NonNullObservableField("")//新固件版本
    val configEnable = NonNullObservableField(false)//是否同步配置
    val backupInfo = NonNullObservableField("")//备份信息
    val failureMainType = NonNullObservableField("")//故障主类型
    val failureChildType = NonNullObservableField("")//故障子类型
    val failureDesc = NonNullObservableField("")//故障描述
    val replaceTime = NonNullObservableField("")//更换时间
    val replacePerson = NonNullObservableField("")//更换人
    val replacePersonPhone = NonNullObservableField("")//更换人电话
}