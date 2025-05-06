package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.core.model.SensorModel
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRRS485Port1SensorParam
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class MR702RS485Port1SingleSensorParamViewModel : ViewModel() {
    val isCustomSensor = NonNullObservableField(false)//是否自定义传感器

    var curSensorModel: SensorModel = SensorModel()
    val sensorParamWrapper = NonNullObservableField(MRRS485Port1SensorParam())

    val modelName = NonNullObservableField("")//物模型/传感器名称
    val modelToken = NonNullObservableField("")//物模型编码
    val address = NonNullObservableField("")
    val baudRate = NonNullObservableField("9600")//波特率
    val dataBit = NonNullObservableField("8")
    val checkBit = NonNullObservableField("")
    val stopBit = NonNullObservableField("")
    val siteType = NonNullObservableField("")//站点类型
    val calculate = NonNullObservableField("")//计算方式
    val sensitivityK = NonNullObservableField("")//灵敏度K
    val temperatureCorrectionCoefficientB = NonNullObservableField("")//温度修正系数 b
    val powValue = NonNullObservableField("")//指数
    val initialFrequencyF0 = NonNullObservableField("")//初始频率 F0
    val initialTemperatureT0 = NonNullObservableField("")//初始温度 T0
    val initialWaterLevel = NonNullObservableField("")//初始水位
    val initialMeasureValue = NonNullObservableField("")//初始测量值
    val initialValue = NonNullObservableField("")//初始测量值

    val modelFieldName = NonNullObservableField("")//采集项名称
    val modelFieldUnit = NonNullObservableField("")//采集项单位
    val hydrologicalIdentification = NonNullObservableField("")//水文标识
    val collectionInstructions = NonNullObservableField("")//采集指令
    val ratio = NonNullObservableField("")//倍率
    val dataFormat = NonNullObservableField("")//数据类型
    val triggerValue = NonNullObservableField("")//触发值
    val upperLimit = NonNullObservableField("")//上限
    val lowerLimit = NonNullObservableField("")//下限
    val correctValue = NonNullObservableField("")//修正值
    val ngateval = NonNullObservableField("")//阈值次数
}