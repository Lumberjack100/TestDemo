package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class MR702RS485Port1SensorAddParamViewModel : ViewModel() {
    val modelField = NonNullObservableField("")
    val sensorAddress = NonNullObservableField("")
    val sensorType = NonNullObservableField("")
    val sensorName = NonNullObservableField("")
    val modelToken = NonNullObservableField("")
    val baudRate = NonNullObservableField("9600")
    val dataBit = NonNullObservableField("5")
    val checkBit = NonNullObservableField("NONE")
    val stopBit = NonNullObservableField("1")

    val hydrologicalIdentification = NonNullObservableField("")//水文识别
    val collectionInstructions = NonNullObservableField("")//采集指令
    val ratio = NonNullObservableField("")// 倍率
    val dataFormat = NonNullObservableField("无符号单字节")//数据类型
    val solutionMethod = NonNullObservableField("加权平均")//解算方法
    val triggerValue = NonNullObservableField("")//触发值
    val upperLimit = NonNullObservableField("")//上限
    val lowerLimit = NonNullObservableField("")//下限
    val correctValue = NonNullObservableField("")//修正值

    val isConfirmBtnVisible = NonNullObservableField(false)
    val saveModelFieldText = NonNullObservableField("保存此采集项")//下限
}