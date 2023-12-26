package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRRS485Port2SensorParam

class MR702RS485Port2SensorParamViewModel : ViewModel() {
    val sensorParamWrapper= NonNullObservableField(MRRS485Port2SensorParam())

    val isEditable = NonNullObservableField(false)
    val channelNumber = NonNullObservableField("")
    val sensorAddress = NonNullObservableField("")
    val sensorType = NonNullObservableField("")
    val sensorName = NonNullObservableField("")
    val modelToken = NonNullObservableField("")
    val hydrologicalIdentification = NonNullObservableField("")//水文识别
    val filterCoefficient = NonNullObservableField("")//滤波系数
    val triggerValue = NonNullObservableField("")//触发值
    val upperLimit = NonNullObservableField("")//上限
    val lowerLimit = NonNullObservableField("")//下限
    val correctValue = NonNullObservableField("")//修正值
}