package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

/**
 * @author：gonghe
 * @time: 2025/1/22
 * @desc: M50 GNSS配置页面 ViewModel
 */
class M50GNSSConfigViewModel : BaseStateViewModel() {
    val samplingRate = NonNullObservableField("") // 采样率
    val elevationAngle = NonNullObservableField("") // 截至高度角

    init {
        // 在所有字段初始化后调用 registerField()
        registerField()
    }

    // 设置初始状态
    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            "samplingRate" to samplingRate.get(),
            "elevationAngle" to elevationAngle.get()
        )
        isDataModified.value = false
        isInitializing = false
    }

    override fun registerField() {
        listOf(
            samplingRate,
            elevationAngle
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
                "samplingRate" -> samplingRate.get() != value
                "elevationAngle" -> elevationAngle.get() != value
                else -> false
            }
        }
    }
}