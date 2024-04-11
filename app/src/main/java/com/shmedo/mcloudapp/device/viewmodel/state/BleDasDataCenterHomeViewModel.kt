package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class BleDasDataCenterHomeViewModel : ViewModel() {
    val isBdCardNumberVisible = NonNullObservableField(false)
    val dataCommunicationMode = NonNullObservableField("")//通讯方式
    val reportingInterval = NonNullObservableField("")//上报间隔
    val bdCardNumber = NonNullObservableField("")//北斗目标卡号
}