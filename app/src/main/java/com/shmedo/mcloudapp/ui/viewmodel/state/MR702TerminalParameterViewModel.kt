package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class MR702TerminalParameterViewModel : ViewModel() {
    val isEditable = NonNullObservableField(false)
    val isReportMethodVisible = NonNullObservableField(true)//
    val isStartTimeItemVisible = NonNullObservableField(true)//

    val reportMethod = NonNullObservableField("定时定点上报")
    val interval = NonNullObservableField("")
    val startTime = NonNullObservableField("")

    val screenRefreshTime = NonNullObservableField("")
    val screenBrightTime = NonNullObservableField("")
    val screenPowerUpTime = NonNullObservableField("")
    val lightness = NonNullObservableField(20)
}