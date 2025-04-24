package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.model.SensorModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

open class MR702PortHomeViewModel : ViewModel() {
    val interfaceName = NonNullObservableField("")
    val interfaceDesc = NonNullObservableField("最多支持32支传感器接入")

    val configPort4851SensorIDToSensorModelMap = mutableMapOf<String, SensorModel>()// key: sensorId，value: 传感器 model


    val configPort4852SensorIDToSensorModelMap = mutableMapOf<String, SensorModel>()// key: sensorName，value: 传感器 model

}