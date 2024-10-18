package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class TimeCalibrationViewModel : ViewModel() {
    //设备时间
    val deviceTime = NonNullObservableField("")

    //系统时间
    val systemTime = NonNullObservableField("")

    //时间差
    val timeDifference = NonNullObservableField("")
}