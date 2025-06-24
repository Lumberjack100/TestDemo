package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class DasIOSensorViewModel : BaseStateViewModel() {
    val mode = NonNullObservableField("关闭")
    val rainResolution = NonNullObservableField("")//雨量计精度
    val supportDumpMInTime = NonNullObservableField(false)//
    val dumpMinTime = NonNullObservableField("")//翻斗翻转最小间隔
    val breakAlarmMode = NonNullObservableField("常开")


    init {
        // 在所有字段初始化后调用 registerField()
        registerField()
    }

    // 设置初始状态
    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            "mode" to mode.get(),
            "rainResolution" to rainResolution.get(),
            "dumpMinTime" to dumpMinTime.get(),
            "breakAlarmMode" to breakAlarmMode.get()
        )
        isDataModified.value = false
        isInitializing = false
    }

    override fun registerField() {
        listOf(
            mode,
            rainResolution,
            dumpMinTime,
            breakAlarmMode
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
                "mode" -> mode.get() != value
                "rainResolution" -> rainResolution.get() != value
                "dumpMinTime" -> dumpMinTime.get() != value
                "breakAlarmMode" -> breakAlarmMode.get() != value
                else -> false
            }
        }
    }
}