package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField
import com.shmedo.lib.device.base.iot_cmd.model.lb20s.LB20SCurrentStateInfo

class LB20SBaseInfoViewModel : ViewModel() {
    val wrapStateInfo = NonNullObservableField(LB20SCurrentStateInfo())

    val solarErrNo = NonNullObservableField("0")//太阳能控制器错误码

    //4G信号强度
    val signalValue = NonNullObservableField(0)

    //北斗 信号强度
    val  bdSinalValue = NonNullObservableField(0)
}