package com.shmedo.mcloudapp.device.viewmodel.state

import com.shmedo.lib.core.base.viewmodel.BaseStateViewModel
import com.shmedo.lib.core.base.viewmodel.NonNullObservableField

class MR702NetworkCommunicationViewModel : BaseStateViewModel() {
    val isEditable = NonNullObservableField(false)
    val isWirelessDisabled = NonNullObservableField(false)//4G模式下不允许操作

    val isWirelessOpened = NonNullObservableField(true)
    val apnName = NonNullObservableField("")
    val userName = NonNullObservableField("")
    val pwd = NonNullObservableField("")

    val isEthernetOpened = NonNullObservableField(true)
    val ipMode = NonNullObservableField("自动")
    val isManualVisible = NonNullObservableField(false)//
    val ip = NonNullObservableField("")
    val subnetMask = NonNullObservableField("")
    val gateway = NonNullObservableField("")
    val preferredDNS = NonNullObservableField("")
    val alternateDNS = NonNullObservableField("")

}