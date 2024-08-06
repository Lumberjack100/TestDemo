package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField
import com.shmedo.lib.cmd.base.iot_cmd.model.common.CommonCurrentStateInfo2

class GW100BaseInfoViewModel : ViewModel() {
    val wrapStateInfo = NonNullObservableField(CommonCurrentStateInfo2())

    val deviceNormal = NonNullObservableField(true)
}