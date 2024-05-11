package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class LR200SensorParamViewModel : ViewModel() {
    val isEditable = NonNullObservableField(true)
}