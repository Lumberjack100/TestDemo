package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class ULSensorParamViewModel : BaseStateViewModel() {
    val initialValue = NonNullObservableField("")//初始值
    val initialization = NonNullObservableField("")//是否初始化
    val initializationMode = NonNullObservableField("")//初始化模式
    val manualValue = NonNullObservableField("")// 手动初始值

    init {
        // 在所有字段初始化后调用 registerField()
        registerField()
    }

    // 设置初始状态
    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            "initializationMode" to initializationMode.get(),
            "manualValue" to manualValue.get()
        )
        isDataModified.value = false
        isInitializing = false
    }

    override fun registerField() {
        listOf(
            initializationMode,
            manualValue
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
                "initializationMode" -> initializationMode.get() != value
                "manualValue" -> manualValue.get() != value
                else -> false
            }
        }
    }
}