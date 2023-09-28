package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class MR702DataCenterHomeViewModel : ViewModel() {
    val status1 = NonNullObservableField("未开启")
    val status2 = NonNullObservableField("未开启")
    val status3 = NonNullObservableField("未开启")
    val status4 = NonNullObservableField("未开启")
    val status5 = NonNullObservableField("未开启")
}