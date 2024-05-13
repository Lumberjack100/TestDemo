package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

open class BaseDeviceStatusInfoParentViewModel : ViewModel() {
    val productName = NonNullObservableField("")
    val productType = NonNullObservableField("")
}