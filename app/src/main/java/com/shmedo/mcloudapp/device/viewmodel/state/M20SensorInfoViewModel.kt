package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class M20SensorInfoViewModel : ViewModel() {
    val memsErrNo = NonNullObservableField("1")//
    val memsAxisX = NonNullObservableField("")//X 轴角度
    val memsAxisY = NonNullObservableField("")//Y 轴角度
    val memsAxisZ = NonNullObservableField("")//Z 轴角度
}