package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class LB20SCustomAlarmTestViewModel : ViewModel() {
    val isEditable = NonNullObservableField(true)
    val broadcastNum = NonNullObservableField("")//
    val broadcastContent = NonNullObservableField("")//
}