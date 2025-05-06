package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.core.model.SensorModel
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRRS485Port1SensorParam
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class MR702RS485Port1SensorParamViewModel : ViewModel() {
    val isEditable = NonNullObservableField(false)
    val sensorParamWrapper= NonNullObservableField(MRRS485Port1SensorParam())
    var curSensorModel: SensorModel = SensorModel()


    val modelName = NonNullObservableField("")
    val modelToken = NonNullObservableField("")
    val address = NonNullObservableField("")
    val baudRate = NonNullObservableField("")
    val dataBit = NonNullObservableField("")
    val checkBit = NonNullObservableField("")
    val stopBit = NonNullObservableField("")

    val modelFieldName = NonNullObservableField("")//采集项名称
    val modelFieldUnit = NonNullObservableField("")//采集项单位
    val hydrologicalIdentification = NonNullObservableField("")//水文标识
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