package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.core.model.DeviceSensorBasicInfo
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class UDSensorDataHistoryViewModel : ViewModel() {
    val startTime = NonNullObservableField("")
    val endTime = NonNullObservableField("")
    val startTimeMills = NonNullObservableField(0L)
    val endTimeMills = NonNullObservableField(0L)

    val modelName = NonNullObservableField("")//

    val modelTokenMap =
        mutableMapOf<String, DeviceSensorBasicInfo>() // key: modelToken，value: 传感器基本信息
}