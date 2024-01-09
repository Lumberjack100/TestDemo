package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class DasTerminalParameterViewModel : ViewModel() {
    val isEditable = NonNullObservableField(false)
    val isReportMethodVisible = NonNullObservableField(true)//
    val isStartTimeItemVisible = NonNullObservableField(false)//

    val reportMethod = NonNullObservableField("定时定点上报")
    val interval = NonNullObservableField("")
    val startTime = NonNullObservableField("")

    val isBdOpened = NonNullObservableField(true)
    val address = NonNullObservableField("")
    val baudRate = NonNullObservableField("")
}