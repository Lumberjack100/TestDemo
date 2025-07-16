package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.kunminx.architecture.domain.message.MutableResult
import com.kunminx.architecture.domain.message.Result
import com.shmedo.core.model.SensorModel
import com.shmedo.mcloudapp.model.MRSensorItem
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

open class MR702PortHomeViewModel : ViewModel() {
    val interfaceName = NonNullObservableField("")
    val interfaceDesc = NonNullObservableField("最多支持32支传感器接入")

    val configPort4851SensorIDToSensorModelMap = mutableMapOf<String, SensorModel>()// key: sensorId，value: 传感器 model
    val configPort4852SensorIDToSensorModelMap = mutableMapOf<String, SensorModel>()// key: sensorName，value: 传感器 model

    // 为不同端口添加对应的事件
    private val _port1SensorUpdateEvent = MutableResult<MRSensorItem>()
    val port1SensorUpdateEvent: Result<MRSensorItem> = _port1SensorUpdateEvent

    private val _port2SensorUpdateEvent = MutableResult<MRSensorItem>()
    val port2SensorUpdateEvent: Result<MRSensorItem> = _port2SensorUpdateEvent


    /**
     * 通知指定端口的传感器更新
     */
    fun notifyPort1SensorUpdate(sensor: MRSensorItem) {
        _port1SensorUpdateEvent.value = sensor
    }

    fun notifyPort2SensorUpdate(sensor: MRSensorItem) {
        _port2SensorUpdateEvent.value = sensor
    }


}