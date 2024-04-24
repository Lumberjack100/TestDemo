package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class LoraSettingViewModel : ViewModel() {
    val isEditable = NonNullObservableField(true)
    val isTargetAddressSupport = NonNullObservableField(true)

    val channel = NonNullObservableField("")//收发频点
    val transmitPower = NonNullObservableField("")//发射功率
    val airSpeed = NonNullObservableField("")//空中速率
    val networkNumber = NonNullObservableField("")//网络编号
    val localAddress = NonNullObservableField("")//本机地址
    val targetAddress = NonNullObservableField("")//目标地址
}