package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.cmd.base.iot_cmd.model.mr.MRRS485Port2SensorParam
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class MR702RS485Port2SensorParamViewModel : ViewModel() {
    val sensorParamWrapper= NonNullObservableField(MRRS485Port2SensorParam())

    val isAdd = NonNullObservableField(false)
    val isEditable = NonNullObservableField(true)

    val sensorType = NonNullObservableField("")
    val modelName = NonNullObservableField("")
    val modelToken = NonNullObservableField("")
    val channelNumber = NonNullObservableField("")
    val hydrologicalIdentification = NonNullObservableField("")//水文标识

    val filterCoefficient = NonNullObservableField("")//滤波系数
    val triggerValue = NonNullObservableField("")//触发值
    val upperLimit = NonNullObservableField("")//上限
    val lowerLimit = NonNullObservableField("")//下限
    val correctValue = NonNullObservableField("")//修正值

    /**
     * 判断当前物模型是否为需要显示高级配置参数的传感器类型
     * 目前支持: 10065, 10066 振弦式传感器
     * @return true 表示需要显示高级参数，false 表示不需要
     */
    fun isAdvancedSensorType(): Boolean {
        val token = modelToken.get()
        return token == "10065" || token == "10066"
    }

    /**
     * 10065, 10066 振弦式传感器特有配置参数
     */
    //是否计算
    val calculate = NonNullObservableField("")
    //计算公式
    val calculateFormula = NonNullObservableField("")
    //灵敏度K
    val sensitivityK = NonNullObservableField("")
    //温度修正系数 b
    val temperatureCorrectionCoefficientB = NonNullObservableField("")
    //初始频率 F0
    val initialFrequencyF0 = NonNullObservableField("")
    //初始温度 T0
    val initialTemperatureT0 = NonNullObservableField("")
    //初始水位
    val initialWaterLevel = NonNullObservableField("")
    //堰角高度
    val weirHeight = NonNullObservableField("")
    //多项式系数A值
    val polyA = NonNullObservableField("")
    //多项式系数B值
    val polyB = NonNullObservableField("")
    //多项式系数C值
    val polyC = NonNullObservableField("")
}