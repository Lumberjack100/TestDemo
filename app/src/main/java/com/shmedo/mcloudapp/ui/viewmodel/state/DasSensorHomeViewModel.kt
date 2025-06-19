package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class DasSensorHomeViewModel : BaseStateViewModel() {
    val isBdOpened = NonNullObservableField(false)
    val address = NonNullObservableField("")
    val baudRate = NonNullObservableField("")

    init {
        // 在所有字段初始化后调用 registerField()
        registerField()
    }

    // 设置初始状态
    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            "isBdOpened" to isBdOpened.get(),
            "address" to address.get(),
            "baudRate" to baudRate.get()
        )
        isDataModified.value = false
        isInitializing = false
    }

    override fun registerField() {
        listOf(
            isBdOpened,address,baudRate
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
                "isBdOpened" -> isBdOpened.get() != value
                "address" -> address.get() != value
                "baudRate" -> baudRate.get() != value
                else -> false
            }
        }
    }
}