package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class LB20SVolumeSettingsViewModel : ViewModel() {
    val isEditable = NonNullObservableField(true)
    val volumeLevel = NonNullObservableField("")//收发频点

}