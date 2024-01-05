package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class FirmwareUpgradeViewModel : ViewModel() {
    val firmwareStatus = NonNullObservableField("")//固件环境代码 0:测试环境 1:运营环境
    val searchText = NonNullObservableField("")
}