package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class MR702RS232Port2ViewModel : BaseStateViewModel() {
    val isOpened = NonNullObservableField(true)
    val address = NonNullObservableField("")
    val baudRate = NonNullObservableField("9600")
    val encodingType = NonNullObservableField("")//编码类型
    val dataLink = NonNullObservableField("")//数据链路
    val inStationConfirm = NonNullObservableField("")//入站确认


    init {
        registerField()
    }

    // 设置初始状态
    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            "isOpened" to isOpened.get(),
            "address" to address.get(),
            "baudRate" to baudRate.get(),
            "encodingType" to encodingType.get(),
            "dataLink" to dataLink.get(),
            "inStationConfirm" to inStationConfirm.get(),
        )
        isDataModified.value = false
        isInitializing = false
    }

    override fun registerField() {
        listOf(
            isOpened,
            address,
            baudRate,
            encodingType,
            dataLink,
            inStationConfirm,
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
                "address" -> address.get() != value
                "baudRate" -> baudRate.get() != value
                "encodingType" -> encodingType.get() != value
                "dataLink" -> dataLink.get() != value
                "inStationConfirm" -> inStationConfirm.get() != value
                else -> false
            }
        }
    }
}