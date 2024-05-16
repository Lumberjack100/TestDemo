package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class M20CommunicationInfoViewModel : ViewModel() {
    val starNum = NonNullObservableField(0)
    val amsState = NonNullObservableField("")

    //4G信号强度
    val signalValue = NonNullObservableField(0)
}