package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class MR702RS232Port1ViewModel : BaseStateViewModel() {
    val status = NonNullObservableField("已接入")
    val isOpened = NonNullObservableField(true)
    val cameraModel = NonNullObservableField("")
    val cameraResolution = NonNullObservableField("")
    val photoInterval = NonNullObservableField("")
    val workModel = NonNullObservableField("")

    val baudRate = NonNullObservableField("")
    val dataBit = NonNullObservableField("")//数据位
    val checkBit = NonNullObservableField("")//校验位
    val stopBit = NonNullObservableField("")//停止位

    init {
        // 在所有字段初始化后调用 registerField()
        registerField()
    }
    
    // 设置初始状态
    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            "isOpened" to isOpened.get(),
            "cameraModel" to cameraModel.get(),
            "cameraResolution" to cameraResolution.get(),
            "photoInterval" to photoInterval.get(),
            "workModel" to workModel.get(),
            "baudRate" to baudRate.get(),
            "dataBit" to dataBit.get(),
            "checkBit" to checkBit.get(),
            "stopBit" to stopBit.get()
        )
        isDataModified.value = false
        isInitializing = false
    }

    override fun registerField() {
        listOf(
            isOpened,
            cameraModel,
            cameraResolution,
            photoInterval,
            workModel,
            baudRate,
            dataBit,
            checkBit,
            stopBit
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
                "cameraModel" -> cameraModel.get() != value
                "cameraResolution" -> cameraResolution.get() != value
                "photoInterval" -> photoInterval.get() != value
                "workModel" -> workModel.get() != value
                "baudRate" -> baudRate.get() != value
                "dataBit" -> dataBit.get() != value
                "checkBit" -> checkBit.get() != value
                "stopBit" -> stopBit.get() != value
                else -> false
            }
        }
    }
}