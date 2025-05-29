package com.shmedo.mcloudapp.ui.viewmodel.state

import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class M20SRadioSettingViewModel : BaseStateViewModel() {
    val isOpened = NonNullObservableField(true)
    val isSupportSwitch = NonNullObservableField(false)

    val rtcmChannel = NonNullObservableField("")//RTCM数据频点
    val receiveChannel = NonNullObservableField("")//广播接收频点
    val sendChannel = NonNullObservableField("")//报警发射频点
    val transmitPower = NonNullObservableField("")//发射功率
    val airSpeed = NonNullObservableField("")//空中速率
    
    init {
        // 在所有字段初始化后调用 registerField()
        registerField()
    }
    
    // 设置初始状态
    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            "isOpened" to isOpened.get(),
            "isSupportSwitch" to isSupportSwitch.get(),
            "rtcmChannel" to rtcmChannel.get(),
            "receiveChannel" to receiveChannel.get(),
            "sendChannel" to sendChannel.get(),
            "transmitPower" to transmitPower.get(),
            "airSpeed" to airSpeed.get()
        )
        isDataModified.value = false
        isInitializing = false
    }
    
    override fun registerField() {
        listOf(
            isOpened,
            isSupportSwitch,
            rtcmChannel,
            receiveChannel,
            sendChannel,
            transmitPower,
            airSpeed
        ).forEach { field ->
            field.addOnPropertyChangedCallback(object : androidx.databinding.Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: androidx.databinding.Observable?, propertyId: Int) {
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
                "isSupportSwitch" -> isSupportSwitch.get() != value
                "rtcmChannel" -> rtcmChannel.get() != value
                "receiveChannel" -> receiveChannel.get() != value
                "sendChannel" -> sendChannel.get() != value
                "transmitPower" -> transmitPower.get() != value
                "airSpeed" -> airSpeed.get() != value
                else -> false
            }
        }
    }


}