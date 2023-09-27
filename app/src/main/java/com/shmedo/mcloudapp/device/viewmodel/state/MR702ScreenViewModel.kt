package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class MR702ScreenViewModel : ViewModel() {
    val isEditable = NonNullObservableField(true)

}