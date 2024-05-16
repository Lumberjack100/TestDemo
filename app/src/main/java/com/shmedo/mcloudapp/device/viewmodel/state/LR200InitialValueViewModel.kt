package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class LR200InitialValueViewModel : ViewModel() {
    val isAutoInit = NonNullObservableField(true)

    val initValue = NonNullObservableField("")
}