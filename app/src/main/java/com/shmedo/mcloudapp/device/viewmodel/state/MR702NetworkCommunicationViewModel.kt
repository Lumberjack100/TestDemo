package com.shmedo.mcloudapp.device.viewmodel.state

import androidx.lifecycle.ViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class MR702NetworkCommunicationViewModel : ViewModel() {
    val isMaskVisible = NonNullObservableField(false)

    val isMobileEnable = NonNullObservableField(false)//4G模式下不允许操作
    val apnName = NonNullObservableField("")
    val userName = NonNullObservableField("")
    val pwd = NonNullObservableField("")

    val isEthernetEnable = NonNullObservableField(true)//
    val isIPContentVisible = NonNullObservableField(false)
    val ipMode = NonNullObservableField("自动")
    val ip = NonNullObservableField("")
    val subnetMask = NonNullObservableField("")
    val gateway = NonNullObservableField("")
    val preferredDNS = NonNullObservableField("")
    val alternateDNS = NonNullObservableField("")

}