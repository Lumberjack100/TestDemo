package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class DasSensorInfoViewModel : ViewModel() {
    //开关量传感器
    val isIOSensorVisible = NonNullObservableField(false)
    val ioType = NonNullObservableField(-1)//
    val ioValue = NonNullObservableField("")//

    //数字水位计
    val isVWPSensorVisible = NonNullObservableField(false)
    val vwpErrNo = NonNullObservableField(0)//
    val vwpValue1 = NonNullObservableField("")//
    val vwpValue2 = NonNullObservableField("")//
    val vwpValue3 = NonNullObservableField("")//

    //MEMS传感器
    val isMEMSSensorVisible = NonNullObservableField(false)
    val memsErrNo = NonNullObservableField(0)//
    val memsAxisX = NonNullObservableField("")//X 轴角度
    val memsAxisY = NonNullObservableField("")//Y 轴角度
    val memsAxisZ = NonNullObservableField("")//Z 轴角度
    val memsAccelerationX = NonNullObservableField("")//X 轴加速度
    val memsAccelerationY = NonNullObservableField("")//Y 轴加速度
    val memsAccelerationZ = NonNullObservableField("")//Z 轴加速度

    val isExternalSensorVisible = NonNullObservableField(false)
}