package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class UDRadioParamViewModel : ViewModel() {
    val isOpened = NonNullObservableField(true)//是否打开

    val receiveChannel = NonNullObservableField("")//报警接收频点
    val sendChannel = NonNullObservableField("")//广播发射频点
    val transmitPower = NonNullObservableField("")//发射功率
    val airSpeed = NonNullObservableField("")//空中速率
}