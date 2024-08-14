package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class LB20SVolumeSettingsViewModel : ViewModel() {
    val isEditable = NonNullObservableField(true)
    val volumeLevel = NonNullObservableField("")//收发频点

}