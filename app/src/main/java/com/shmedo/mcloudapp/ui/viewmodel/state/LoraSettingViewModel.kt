package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class LoraSettingViewModel : BaseStateViewModel() {
    /** 是否支持目标地址配置 */
    val isTargetAddressSupport = NonNullObservableField(true)

    /** 是否支持 LORA 型号配置（仅 MG301 设备显示） */
    val isLoraTypeSupport = NonNullObservableField(false)

    val loraType = NonNullObservableField("")//LORA型号
    val channel = NonNullObservableField("")//收发频点/通讯信道
    val transmitPower = NonNullObservableField("")//发射功率
    val airSpeed = NonNullObservableField("")//空中速率
    val networkNumber = NonNullObservableField("")//网络编号
    val localAddress = NonNullObservableField("")//本机地址
    val targetAddress = NonNullObservableField("")//目标地址

    init {
        // 在所有字段初始化后调用 registerField()
        registerField()
    }

    // 设置初始状态
    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            "loraType" to loraType.get(),
            "channel" to channel.get(),
            "transmitPower" to transmitPower.get(),
            "airSpeed" to airSpeed.get(),
            "networkNumber" to networkNumber.get(),
            "localAddress" to localAddress.get(),
            "targetAddress" to targetAddress.get()
        )
        isDataModified.value = false
        isInitializing = false
    }

    override fun registerField() {
        listOf(
            loraType,
            channel,
            transmitPower,
            airSpeed,
            networkNumber,
            localAddress,
            targetAddress
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
                "loraType" -> loraType.get() != value
                "channel" -> channel.get() != value
                "transmitPower" -> transmitPower.get() != value
                "airSpeed" -> airSpeed.get() != value
                "networkNumber" -> networkNumber.get() != value
                "localAddress" -> localAddress.get() != value
                "targetAddress" -> targetAddress.get() != value
                else -> false
            }
        }
    }
}