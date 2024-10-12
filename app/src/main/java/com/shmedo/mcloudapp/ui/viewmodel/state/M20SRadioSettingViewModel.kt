package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class M20SRadioSettingViewModel : ViewModel() {
    val isOpened = NonNullObservableField(true)
    val isSupportSwitch = NonNullObservableField(false)

    val rtcmChannel = NonNullObservableField("")//RTCM数据频点
    val receiveChannel = NonNullObservableField("")//广播接收频点
    val sendChannel = NonNullObservableField("")//报警发射频点
    val transmitPower = NonNullObservableField("")//发射功率
    val airSpeed = NonNullObservableField("")//空中速率
}