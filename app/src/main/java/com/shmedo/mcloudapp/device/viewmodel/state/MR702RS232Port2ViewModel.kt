package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class MR702RS232Port2ViewModel : ViewModel() {
    val status = NonNullObservableField("已接入")
    val isOpened = NonNullObservableField(true)
    val address = NonNullObservableField("")
    val baudRate = NonNullObservableField("")
    val dataBit = NonNullObservableField("")
    val checkBit = NonNullObservableField("")
    val stopBit = NonNullObservableField("")
}