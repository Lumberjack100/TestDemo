package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField
import com.shmedo.lib.device.base.iot_cmd.model.common.CommonCurrentStateInfo2

class UProductBaseInfoViewModel : ViewModel() {
    val wrapStateInfo = NonNullObservableField(CommonCurrentStateInfo2())

    val deviceNormal = NonNullObservableField(true)
    val isRunningDataVisible = NonNullObservableField(false)

}