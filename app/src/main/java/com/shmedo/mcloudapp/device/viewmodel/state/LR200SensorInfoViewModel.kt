package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class LR200SensorInfoViewModel : ViewModel() {
    //MEMS传感器
    val isMEMSSensorVisible = NonNullObservableField(false)
    val name = NonNullObservableField("")
    val memsErrNo = NonNullObservableField(0)//
    val memsAxisX = NonNullObservableField("-1")//X 轴角度
    val memsAxisY = NonNullObservableField("-1")//Y 轴角度
    val memsAxisZ = NonNullObservableField("-1")//Z 轴角度
}