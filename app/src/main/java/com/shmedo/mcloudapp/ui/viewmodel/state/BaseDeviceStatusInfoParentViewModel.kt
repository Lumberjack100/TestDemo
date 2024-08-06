package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

open class BaseDeviceStatusInfoParentViewModel : ViewModel() {
    val productName = NonNullObservableField("")
    val productType = NonNullObservableField("")
}