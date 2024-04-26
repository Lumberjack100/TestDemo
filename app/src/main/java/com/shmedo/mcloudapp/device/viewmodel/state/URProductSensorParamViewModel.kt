package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class URProductSensorParamViewModel : ViewModel() {
    val isEditable = NonNullObservableField(true)

    val rainResolution = NonNullObservableField("")//雨量计精度
}