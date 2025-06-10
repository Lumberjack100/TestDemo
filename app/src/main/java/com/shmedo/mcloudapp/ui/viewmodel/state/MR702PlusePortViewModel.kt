package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import androidx.lifecycle.ViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class MR702PlusePortViewModel : BaseStateViewModel() {
    val isOpened = NonNullObservableField(true)//功能开关
    val workMode = NonNullObservableField("计数")//功能选择 计数 消警
    val pulseResolution = NonNullObservableField("1")//脉冲分辨率 默认1，整型，大于0，最大9999
    val debounceCoefficient = NonNullObservableField("5")//消抖系数 默认5，整型，大于0，最大60.单位s

    init {
        registerField()
    }

    // 设置初始状态
    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            "isOpened" to isOpened.get(),
            "workMode" to workMode.get(),
            "pulseResolution" to pulseResolution.get(),
            "debounceCoefficient" to debounceCoefficient.get()
        )
        isDataModified.value = false
        isInitializing = false
    }

    override fun registerField() {
        listOf(
            isOpened,
            workMode,
            pulseResolution,
            debounceCoefficient
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
                "workMode" -> workMode.get() != value
                "pulseResolution" -> pulseResolution.get() != value
                "debounceCoefficient" -> debounceCoefficient.get() != value
                else -> false
            }
        }
    }
}