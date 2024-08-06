package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class URProductSensorParamViewModel : ViewModel() {
    val isEditable = NonNullObservableField(true)

    val rainResolution = NonNullObservableField("")//雨量计精度
}