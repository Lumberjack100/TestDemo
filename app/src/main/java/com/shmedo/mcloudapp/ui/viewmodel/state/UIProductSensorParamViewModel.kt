package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class UIProductSensorParamViewModel : ViewModel() {
    val isEditable = NonNullObservableField(true)

    val measureInterval = NonNullObservableField("")//测量间隔
    val averageTimes = NonNullObservableField("")//平均次数

}