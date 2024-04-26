package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class URProductSensorInfoViewModel : ViewModel() {
    //雨量计传感器状态
    val rainErrNo = NonNullObservableField("1")

    //24小时雨量值
    val rain24h = NonNullObservableField("")
}