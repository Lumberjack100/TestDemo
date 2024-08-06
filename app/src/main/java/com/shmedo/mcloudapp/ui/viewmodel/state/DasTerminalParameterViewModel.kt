package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class DasTerminalParameterViewModel : ViewModel() {
    val isEditable = NonNullObservableField(true)
    val isStartTimeItemVisible = NonNullObservableField(false)//

    val reportMethod = NonNullObservableField("定时定点上报")
    val interval = NonNullObservableField("")
    val startTime = NonNullObservableField("")
}