package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class M50LORASettingViewModel : BaseStateViewModel() {
    val isOpened = NonNullObservableField(true)
    val rtcmChannel = NonNullObservableField("")//RTCM数据频点
    val alarmChannel = NonNullObservableField("")//报警频点
    val localAddress = NonNullObservableField("")//本机地址

    init {
        // 在所有字段初始化后调用 registerField()
        registerField()
    }

    // 设置初始状态
    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            "isOpened" to isOpened.get(),
            "rtcmChannel" to rtcmChannel.get(),
            "alarmChannel" to alarmChannel.get(),
            "localAddress" to localAddress.get()
        )
        isDataModified.value = false
        isInitializing = false
    }

    override fun registerField() {
        listOf(
            isOpened,
            rtcmChannel,
            alarmChannel,
            localAddress
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
                "isOpened" -> isOpened.get() != value
                "rtcmChannel" -> rtcmChannel.get() != value
                "alarmChannel" -> alarmChannel.get() != value
                "localAddress" -> localAddress.get() != value
                else -> false
            }
        }
    }
}