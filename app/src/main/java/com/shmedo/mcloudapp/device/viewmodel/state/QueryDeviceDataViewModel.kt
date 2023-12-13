package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class QueryDeviceDataViewModel : ViewModel() {
    val sn = NonNullObservableField("")
    val platformType = NonNullObservableField("")
    val dataType = NonNullObservableField("")
    val periodDate = NonNullObservableField("")
    val regexContent = NonNullObservableField("")
}