package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class UDRadioParamViewModel : BaseStateViewModel() {
    val isOpened = NonNullObservableField(true)//是否打开

    val receiveChannel = NonNullObservableField("")//报警接收频点
    val sendChannel = NonNullObservableField("")//广播发射频点
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
            receiveChannel,
            sendChannel,
            transmitPower,
            airSpeed
        ).forEach {
            it.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    if (!isInitializing) {
                        isDataModified.value = true
                    }
                }
            })
        }
    }

    override fun updateModificationStatus() {
        isDataModified.value = initialState != mapOf(
            "isOpened" to isOpened.get(),
            "receiveChannel" to receiveChannel.get(),
            "sendChannel" to sendChannel.get(),
            "transmitPower" to transmitPower.get(),
            "airSpeed" to airSpeed.get()
        )
    }
}