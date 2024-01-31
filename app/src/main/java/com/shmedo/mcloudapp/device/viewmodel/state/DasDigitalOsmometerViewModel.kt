package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class DasDigitalOsmometerViewModel : ViewModel() {
    val isOpened = NonNullObservableField(false)
    val address = NonNullObservableField("")//地址
    val triggerValue = NonNullObservableField("")//触发值
    val correctValue = NonNullObservableField("")//修正值
    val wireRopeLength = NonNullObservableField("")//绳长度
    val nozzelHeight = NonNullObservableField("")//高程
}