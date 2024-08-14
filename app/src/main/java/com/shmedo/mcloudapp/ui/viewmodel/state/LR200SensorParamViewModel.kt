package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class LR200SensorParamViewModel : ViewModel() {
    val isEditable = NonNullObservableField(true)
}