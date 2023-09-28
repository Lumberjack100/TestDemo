package com.shmedo.mcloudapp.device.viewmodel.state

import com.shmedo.lib.core.base.viewmodel.BaseStateViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class MR702TerminalParameterViewModel : BaseStateViewModel() {
    val isEditable = NonNullObservableField(false)
    val isReportMethodVisible = NonNullObservableField(true)//

    val reportMethod = NonNullObservableField("定时定点上报")
    val interval = NonNullObservableField("")
    val startTime = NonNullObservableField("")

    val screenRefreshTime = NonNullObservableField("")
    val screenBrightTime = NonNullObservableField("")
    val screenPowerUpTime = NonNullObservableField("")
    val lightness = NonNullObservableField(20)
}