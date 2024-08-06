package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRRS485Port1SensorParam

class MR702RS485Port1SensorParamViewModel : ViewModel() {
    val sensorParamWrapper= NonNullObservableField(MRRS485Port1SensorParam())

    val isEditable = NonNullObservableField(false)
    val sensorAddress = NonNullObservableField("")
    val sensorType = NonNullObservableField("")
    val sensorName = NonNullObservableField("")
    val modelToken = NonNullObservableField("")
    val baudRate = NonNullObservableField("")
    val dataBit = NonNullObservableField("")
    val checkBit = NonNullObservableField("")
    val stopBit = NonNullObservableField("")

    val hydrologicalIdentification = NonNullObservableField("")//水文识别
    val collectionInstructions= NonNullObservableField("")//采集指令
    val ratio = NonNullObservableField("9600")// 倍率
    val dataFormat = NonNullObservableField("")//数据类型
    val solutionMethod = NonNullObservableField("")//解算方法
    val triggerValue = NonNullObservableField("")//触发值
    val upperLimit = NonNullObservableField("")//上限
    val lowerLimit = NonNullObservableField("")//下限
    val correctValue = NonNullObservableField("")//修正值
    val ngateval = NonNullObservableField("")//阈值次数

}