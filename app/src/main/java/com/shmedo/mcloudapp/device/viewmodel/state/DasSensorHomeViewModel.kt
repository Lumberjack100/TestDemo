package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class DasSensorHomeViewModel : ViewModel() {
    val isBdOpened = NonNullObservableField(false)
    val address = NonNullObservableField("")
    val baudRate = NonNullObservableField("")
}