package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class UDMobileNetworkParamViewModel : BaseStateViewModel() {
    val apnName = NonNullObservableField("")
    val userName = NonNullObservableField("")
    val pwd = NonNullObservableField("")


    init {
        // 在所有字段初始化后调用 registerField()
        registerField()
    }

    // 设置初始状态
    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            "apnName" to apnName.get(),
            "userName" to userName.get(),
            "pwd" to pwd.get()
        )
        isDataModified.value = false
        isInitializing = false
    }

    override fun registerField() {
        listOf(
            apnName,
            userName,
            pwd
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
                "apnName" -> apnName.get() != value
                "userName" -> userName.get() != value
                "pwd" -> pwd.get() != value
                else -> false
            }
        }
    }
}