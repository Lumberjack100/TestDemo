package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField


class DasDigitalOsmometerViewModel : BaseStateViewModel() {

    val isOpened = NonNullObservableField(false)
    val address = NonNullObservableField("")//地址
    val triggerValue = NonNullObservableField("")//触发值
    val correctValue = NonNullObservableField("")//修正值
    val wireRopeLength = NonNullObservableField("")//绳长度
    val installElevation = NonNullObservableField("")//高程

    init {
        // 在所有字段初始化后调用 registerField()
        registerField()
    }

    // 设置初始状态
    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            "isOpened" to isOpened.get(),
            "address" to address.get(),
            "triggerValue" to triggerValue.get(),
            "correctValue" to correctValue.get(),
            "wireRopeLength" to wireRopeLength.get(),
            "installElevation" to installElevation.get()
        )
        isDataModified.value = false
        isInitializing = false
    }

    override fun registerField() {
        listOf(
            isOpened,
            address,
            triggerValue,
            correctValue,
            wireRopeLength,
            installElevation
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
                "triggerValue" -> triggerValue.get() != value
                "correctValue" -> correctValue.get() != value
                "wireRopeLength" -> wireRopeLength.get() != value
                "installElevation" -> installElevation.get() != value
                else -> false
            }
        }
    }
}