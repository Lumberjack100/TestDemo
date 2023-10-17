package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

open class MR702InterfaceHomeViewModel : ViewModel() {
    val isEditable = NonNullObservableField(false)

    val interfaceName = NonNullObservableField("")
    val interfaceDesc = NonNullObservableField("最多支持32支传感器接入")
}