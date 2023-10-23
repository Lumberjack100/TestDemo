package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class MR702RainPortViewModel : ViewModel() {
    val status = NonNullObservableField("已接入")
    val isOpened = NonNullObservableField(true)
    val rainResolution = NonNullObservableField("")
    val debounceCoefficient = NonNullObservableField("")
}