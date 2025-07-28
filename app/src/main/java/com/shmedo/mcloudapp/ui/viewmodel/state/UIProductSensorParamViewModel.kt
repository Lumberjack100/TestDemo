package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class UIProductSensorParamViewModel : BaseStateViewModel() {
    val measureInterval = NonNullObservableField("")//测量间隔
    val averageTimes = NonNullObservableField("")//平均次数

    init {
        // 在所有字段初始化后调用 registerField()
        registerField()
    }

    // 设置初始状态
    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            "measureInterval" to measureInterval.get(),
            "averageTimes" to averageTimes.get()
        )
        isDataModified.value = false
        isInitializing = false
    }

    override fun registerField() {
        listOf(
            measureInterval,averageTimes
        ).forEach {
            it.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    if (!isInitializing) {
                        isDataModified.value = true
                    }
                }
            })
        }
    }

    override fun updateModificationStatus() {
        isDataModified.value = initialState != mapOf(
            "measureInterval" to measureInterval.get(),
            "averageTimes" to averageTimes.get()
        )
    }
}