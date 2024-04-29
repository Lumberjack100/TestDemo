package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class UIProductSensorInfoViewModel : ViewModel() {
    val memsErrNo = NonNullObservableField("1")//

    //X 轴初始角度
    val memsInitialAxisX = NonNullObservableField("")

    //Y 轴初始角度
    val memsInitialAxisY = NonNullObservableField("")

    //Z 轴初始角度
    val memsInitialAxisZ = NonNullObservableField("")

    //X 轴当前角度
    val memsAxisXCurrent = NonNullObservableField("")

    //Y 轴当前角度
    val memsAxisYCurrent = NonNullObservableField("")

    //Z 轴当前角度
    val memsAxisZCurrent = NonNullObservableField("")

    //X 轴加速度
    val memsAxisXAcceleration = NonNullObservableField("")

    //Y 轴加速度
    val memsAxisYAcceleration = NonNullObservableField("")

    //Z 轴加速度
    val memsAxisZAcceleration = NonNullObservableField("")

    //连续运行时间
    val runTime = NonNullObservableField("")
}