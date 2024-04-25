package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class M20SRadioSettingViewModel : ViewModel() {
    val isEditable = NonNullObservableField(true)

    val rtcmChannel = NonNullObservableField("")//RTCM数据频点
    val receiveChannel = NonNullObservableField("")//报警接收频点
    val sendChannel = NonNullObservableField("")//广播发射频点
    val transmitPower = NonNullObservableField("")//发射功率
    val airSpeed = NonNullObservableField("")//空中速率
}