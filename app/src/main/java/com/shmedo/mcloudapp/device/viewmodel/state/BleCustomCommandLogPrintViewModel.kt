package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class BleCustomCommandLogPrintViewModel : ViewModel() {
    val debugMode = NonNullObservableField("关")
    val command = NonNullObservableField("")

}