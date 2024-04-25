package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class M20SWorkModelViewModel : ViewModel() {
    val isEditable = NonNullObservableField(true)

    val model = NonNullObservableField("")//模式
    val frequency = NonNullObservableField("")//频率
}