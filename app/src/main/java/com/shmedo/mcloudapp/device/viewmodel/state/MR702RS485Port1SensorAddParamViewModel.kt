package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField
import com.shmedo.mcloudapp.device.model.ModelField
import com.shmedo.mcloudapp.device.model.SensorModel

class MR702RS485Port1SensorAddParamViewModel : ViewModel() {
    val curSensorModel: SensorModel = SensorModel()
    val curModelFieldList: MutableList<ModelField> = mutableListOf()

    val isCustomSensor = NonNullObservableField(false)//是否自定义传感器
    val isFirstModelField = NonNullObservableField(true)//是否第一个采集项
    val isSaveModelFieldBtnVisible = NonNullObservableField(true)
    val saveModelFieldBtnText = NonNullObservableField("保存此采集项")
    val isConfirmBtnVisible = NonNullObservableField(false)

    val sensorType = NonNullObservableField("")
    val sensorName = NonNullObservableField("")//物模型/传感器名称
    val modelToken = NonNullObservableField("")//物模型编码
    val sensorAddress = NonNullObservableField("")
    val baudRate = NonNullObservableField("9600")//波特率
    val dataBit = NonNullObservableField("8")
    val checkBit = NonNullObservableField("")
    val stopBit = NonNullObservableField("")

    val modelFieldName = NonNullObservableField("")//采集项名称
    val modelFieldUnit = NonNullObservableField("")//采集项单位
    val hydrologicalIdentification = NonNullObservableField("")//水文识别
    val collectionInstructions = NonNullObservableField("")//采集指令
    val ratio = NonNullObservableField("")// 倍率
    val dataFormat = NonNullObservableField("")//数据类型
    val solutionMethod = NonNullObservableField("")//解算方法
    val triggerValue = NonNullObservableField("")//触发值
    val upperLimit = NonNullObservableField("")//上限
    val lowerLimit = NonNullObservableField("")//下限
    val correctValue = NonNullObservableField("")//修正值
    val ngateval = NonNullObservableField("")//阈值次数
}