package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class LB20SCustomAlarmTestViewModel : ViewModel() {
    val isEditable = NonNullObservableField(true)
    val broadcastNum = NonNullObservableField("")//
    val broadcastContent = NonNullObservableField("")//
}