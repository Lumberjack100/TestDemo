package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.kunminx.architecture.domain.message.MutableResult
import com.shmedo.mcloudapp.model.SensorModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

open class MR702PortHomeViewModel : ViewModel() {
    val isEditable = MutableResult(false)

    val interfaceName = NonNullObservableField("")
    val interfaceDesc = NonNullObservableField("最多支持32支传感器接入")

    val configPortSensorModelListMap =
        mutableMapOf<String, MutableList<SensorModel>>() // key: 串口名称，value: 传感器 model列表

    val configPort4851SensorNameToSensorModelMap =
        mutableMapOf<String, SensorModel>() // key: sensorName，value: 传感器 model

    val configPort4852SensorTypeToSensorModelMap =
        mutableMapOf<String, SensorModel>()// key: sensorName，value: 传感器 model

    val modelTokenToSensorModelMap = mutableMapOf<String, SensorModel>()// key: modelToken，value: 传感器 model
}