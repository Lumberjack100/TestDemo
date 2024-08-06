package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.kunminx.architecture.domain.message.MutableResult
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField
import com.shmedo.mcloudapp.model.SensorModel

open class MR702PortHomeViewModel : ViewModel() {
    val isEditable = MutableResult(false)

    val interfaceName = NonNullObservableField("")
    val interfaceDesc = NonNullObservableField("最多支持32支传感器接入")

    val portSensorModelListMap = mutableMapOf<String, MutableList<SensorModel>>() // key: 串口名称，value: 传感器 model列表
    val sensorModelMap = mutableMapOf<String, SensorModel>() // key: sensorType，value: 传感器 model
}