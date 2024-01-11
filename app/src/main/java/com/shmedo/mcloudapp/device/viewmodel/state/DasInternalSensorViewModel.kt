package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class DasInternalSensorViewModel : ViewModel() {
    val isEditable = NonNullObservableField(false)
}