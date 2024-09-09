package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class UDAltitudeParamViewModel : ViewModel() {
    val altitudeMeasureMode = NonNullObservableField("")//模式
    val altitude = NonNullObservableField("")//海拔
}