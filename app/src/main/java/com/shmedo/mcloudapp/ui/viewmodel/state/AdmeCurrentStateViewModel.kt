package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class AdmeCurrentStateViewModel : ViewModel() {
    val ctrMotionInfoVisible = NonNullObservableField(false)
    val isMotorInfoNormal = NonNullObservableField(true)
    val measureMode = NonNullObservableField("")
    val motorInfo = NonNullObservableField("")
    val measurePoint = NonNullObservableField("")

    val signalValue = NonNullObservableField(0)
}