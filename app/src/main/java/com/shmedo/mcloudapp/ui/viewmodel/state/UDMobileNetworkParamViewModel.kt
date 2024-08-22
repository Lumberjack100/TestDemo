package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class UDMobileNetworkParamViewModel : ViewModel() {
    val isEditable = NonNullObservableField(true)

    val apnName = NonNullObservableField("")
    val userName = NonNullObservableField("")
    val pwd = NonNullObservableField("")
}