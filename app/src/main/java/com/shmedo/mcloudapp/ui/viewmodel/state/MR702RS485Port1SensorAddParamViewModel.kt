package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.model.ModelField
import com.shmedo.mcloudapp.model.SensorModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class MR702RS485Port1SensorAddParamViewModel : ViewModel() {
    var curSensorModel: SensorModel = SensorModel()
    val curModelFieldList: MutableList<ModelField> = mutableListOf()

    val isCustomSensor = NonNullObservableField(false)//是否自定义传感器
    val isFirstModelField = NonNullObservableField(true)//是否第一个采集项
    val isSaveModelFieldBtnVisible = NonNullObservableField(true)//是否显示保存采集项按钮
    val saveModelFieldBtnText = NonNullObservableField("保存此采集项")
    val isConfirmBtnVisible = NonNullObservableField(false)//是否显示确认按钮

    val modelName = NonNullObservableField("")//物模型/传感器名称
    val modelToken = NonNullObservableField("")//物模型编码
    val address = NonNullObservableField("")
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