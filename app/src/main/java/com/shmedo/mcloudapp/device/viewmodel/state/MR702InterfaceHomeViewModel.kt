package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.kunminx.architecture.domain.message.MutableResult
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField
import com.shmedo.mcloudapp.device.model.SensorModel

open class MR702InterfaceHomeViewModel : ViewModel() {
    val isEditable = MutableResult(false)

    val interfaceName = NonNullObservableField("")
    val interfaceDesc = NonNullObservableField("最多支持32支传感器接入")

    val sensorConfig = mutableMapOf<String, List<SensorModel>>() // key: 串口名称，value: 传感器 model列表
    val sensorModelMap = mutableMapOf<String, SensorModel>() // key: modelToken，value: 传感器 model
}