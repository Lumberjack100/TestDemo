package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class UDSerialPortParamViewModel : BaseStateViewModel() {
    val rs485Enable = NonNullObservableField(true)//RS485 端口启用
    val rs485BaudRate = NonNullObservableField("")//波特率
    val rs485Address = NonNullObservableField("")//本机地址

    val rainGaugeEnable = NonNullObservableField(true)//雨量计端口启用
    val rainGaugeResolution = NonNullObservableField("")//雨量计分辨率
    val rainGaugeTotalValue = NonNullObservableField("")//累计雨量值


    init {
        // 在所有字段初始化后调用 registerField()
        registerField()
    }

    // 设置初始状态
    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            "rs485Enable" to rs485Enable.get(),
            "rs485BaudRate" to rs485BaudRate.get(),
            "rs485Address" to rs485Address.get(),
            "rainGaugeEnable" to rainGaugeEnable.get(),
            "rainGaugeResolution" to rainGaugeResolution.get()
        )
        isDataModified.value = false
        isInitializing = false
    }

    override fun registerField() {
        listOf(
            rs485Enable,
            rs485BaudRate,
            rs485Address,
            rainGaugeEnable,
            rainGaugeResolution
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
                "rs485Enable" -> rs485Enable.get() != value
                "rs485BaudRate" -> rs485BaudRate.get() != value
                "rs485Address" -> rs485Address.get() != value
                "rainGaugeEnable" -> rainGaugeEnable.get() != value
                "rainGaugeResolution" -> rainGaugeResolution.get() != value
                else -> false
            }
        }
    }

}