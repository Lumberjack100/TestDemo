package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class DasCollectorSettingViewModel : BaseStateViewModel() {

    val isEditable = NonNullObservableField(true)
    val isShowSensitivity = NonNullObservableField(false)

    val type = NonNullObservableField("")//采集器型号
    val collectorAddress = NonNullObservableField("")//采集器地址
    val solvingInterval = NonNullObservableField("")//采集器解算间隔
    val standbyTime = NonNullObservableField("")//待机时间
    val collectionInterval = NonNullObservableField("")//采集器采集间隔
    val sensitivity = NonNullObservableField("")//灵敏度

    init {
        // 在所有字段初始化后调用 registerField()
        registerField()
    }

    // 设置初始状态
    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            "type" to type.get(),
            "collectorAddress" to collectorAddress.get(),
            "solvingInterval" to solvingInterval.get(),
            "standbyTime" to standbyTime.get(),
            "collectionInterval" to collectionInterval.get(),
            "sensitivity" to sensitivity.get()
        )
        isDataModified.value = false
        isInitializing = false
    }

    override fun registerField() {
        listOf(
            type,
            collectorAddress,
            solvingInterval,
            standbyTime,
            collectionInterval,
            sensitivity
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
                "type" -> type.get() != value
                "collectorAddress" -> collectorAddress.get() != value
                "solvingInterval" -> solvingInterval.get() != value
                "standbyTime" -> standbyTime.get() != value
                "collectionInterval" -> collectionInterval.get() != value
                "sensitivity" -> sensitivity.get() != value
                else -> false
            }
        }
    }
}