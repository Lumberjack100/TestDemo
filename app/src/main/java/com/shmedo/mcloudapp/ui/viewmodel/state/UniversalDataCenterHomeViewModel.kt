package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

/**
 * 创建者：gonghe
 * 创建时间：2024/4/28
 * 描述： TODO
 */
class UniversalDataCenterHomeViewModel : BaseStateViewModel() {
    val isSupportedReportInterval = NonNullObservableField(false)
    val reportInterval = NonNullObservableField("")


    init {
        // 在所有字段初始化后调用 registerField()
        registerField()
    }

    // 设置初始状态
    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            "isSupportedReportInterval" to isSupportedReportInterval.get(),
            "reportInterval" to reportInterval.get()
        )
        isDataModified.value = false
        isInitializing = false
    }

    override fun registerField() {
        listOf(
            isSupportedReportInterval,
            reportInterval
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
                "isSupportedReportInterval" -> isSupportedReportInterval.get() != value
                "reportInterval" -> reportInterval.get() != value
                else -> false
            }
        }
    }
}