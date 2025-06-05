package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class MR702NetworkConfigViewModel : BaseStateViewModel() {
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

    init {
        // 在所有字段初始化后调用 registerField()
        registerField()
    }
    
    // 设置初始状态
    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            "isWirelessDisabled" to isWirelessDisabled.get(),
            "isWirelessOpened" to isWirelessOpened.get(),
            "apnName" to apnName.get(),
            "userName" to userName.get(),
            "pwd" to pwd.get(),
            "isEthernetOpened" to isEthernetOpened.get(),
            "ipMode" to ipMode.get(),
            "isManualVisible" to isManualVisible.get(),
            "ip" to ip.get(),
            "subnetMask" to subnetMask.get(),
            "gateway" to gateway.get(),
            "preferredDNS" to preferredDNS.get(),
            "alternateDNS" to alternateDNS.get()
        )
        isDataModified.value = false
        isInitializing = false
    }

    override fun registerField() {
        listOf(
            isWirelessDisabled,
            isWirelessOpened,
            apnName,
            userName,
            pwd,
            isEthernetOpened,
            ipMode,
            isManualVisible,
            ip,
            subnetMask,
            gateway,
            preferredDNS,
            alternateDNS
        ).forEach { field ->
            field.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    updateModificationStatus()
                }
            })
        }
    }

    override fun updateModificationStatus() {
        if (isInitializing) return
        isDataModified.value = initialState.any { (key, value) ->
            when (key) {
                "isWirelessDisabled" -> isWirelessDisabled.get() != value
                "isWirelessOpened" -> isWirelessOpened.get() != value
                "apnName" -> apnName.get() != value
                "userName" -> userName.get() != value
                "pwd" -> pwd.get() != value
                "isEthernetOpened" -> isEthernetOpened.get() != value
                "ipMode" -> ipMode.get() != value
                "isManualVisible" -> isManualVisible.get() != value
                "ip" -> ip.get() != value
                "subnetMask" -> subnetMask.get() != value
                "gateway" -> gateway.get() != value
                "preferredDNS" -> preferredDNS.get() != value
                "alternateDNS" -> alternateDNS.get() != value
                else -> false
            }
        }
    }
}