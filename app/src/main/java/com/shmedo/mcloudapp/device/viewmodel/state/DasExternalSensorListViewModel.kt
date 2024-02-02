package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField
import com.shmedo.lib.device.base.iot_cmd.model.das.DasExternalSensorInfo

class DasExternalSensorListViewModel : ViewModel() {
    val isSubmitBtnVisible = NonNullObservableField(false)//是否显示提交按钮
    val isVibratingWireSensor = NonNullObservableField(false)//是否振弦式传感器
    val collectorType = NonNullObservableField<String>("")//采集器型号
    val sensorModelMap = mutableMapOf<String, DasExternalSensorInfo>() // key: 地址或通道号
}