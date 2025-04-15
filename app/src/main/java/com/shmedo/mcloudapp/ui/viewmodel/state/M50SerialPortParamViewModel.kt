package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class M50SerialPortParamViewModel : BaseStateViewModel() {
    val isExternalPower = NonNullObservableField(false) //外部供电

    val rs232ExternalDevice = NonNullObservableField("")//232 外接设备
    val captureFrequency = NonNullObservableField("")//抓拍频率
    val imageResolution = NonNullObservableField("")//图片分辨率
    val rs485ExternalDevice = NonNullObservableField("")//485 外接设备
    val rs485BaudRate = NonNullObservableField("")//485 波特率
    val rs485ExternalDeviceAddr = NonNullObservableField("")//485 外接设备地址

    init {
        // 在所有字段初始化后调用 registerField()
        registerField()
    }
    
    // 设置初始状态
    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            "isExternalPower" to isExternalPower.get(),
            "rs232ExternalDevice" to rs232ExternalDevice.get(),
            "captureFrequency" to captureFrequency.get(),
            "imageResolution" to imageResolution.get(),
            "rs485ExternalDevice" to rs485ExternalDevice.get(),
            "rs485BaudRate" to rs485BaudRate.get(),
            "rs485ExternalDeviceAddr" to rs485ExternalDeviceAddr.get()
        )
        isDataModified.value = false
        isInitializing = false
    }

    override fun registerField() {
        listOf(
            rs232ExternalDevice,
            captureFrequency,
            imageResolution,
            rs485ExternalDevice,
            rs485BaudRate,
            rs485ExternalDeviceAddr
        ).forEach { field ->
            field.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    updateModificationStatus()
                }
            })
        }
        
        // 为 isExternalPower 单独添加监听器，因为它是布尔类型
        isExternalPower.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                updateModificationStatus()
            }
        })
    }

    override fun updateModificationStatus() {
        if (isInitializing) return
        isDataModified.value = initialState.any { (key, value) ->
            when (key) {
                "isExternalPower" -> isExternalPower.get() != value
                "rs232ExternalDevice" -> rs232ExternalDevice.get() != value
                "captureFrequency" -> captureFrequency.get() != value
                "imageResolution" -> imageResolution.get() != value
                "rs485ExternalDevice" -> rs485ExternalDevice.get() != value
                "rs485BaudRate" -> rs485BaudRate.get() != value
                "rs485ExternalDeviceAddr" -> rs485ExternalDeviceAddr.get() != value
                else -> false
            }
        }
    }
}