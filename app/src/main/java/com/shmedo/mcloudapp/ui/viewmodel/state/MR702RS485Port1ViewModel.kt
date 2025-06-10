package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class MR702RS485Port1ViewModel : BaseStateViewModel() {
    val acquisitionFrequency = NonNullObservableField("500")//采集频率
    val collectionDuration = NonNullObservableField("5")//采集周期
    val collectionTimes = NonNullObservableField("1")//采集次数
    val noResponseTimes = NonNullObservableField("3")//无应答次数
    val delayDuration = NonNullObservableField("10")//延时时间

    init {
        // 在所有字段初始化后调用 registerField()
        registerField()
    }

    // 设置初始状态
    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            "acquisitionFrequency" to acquisitionFrequency.get(),
            "collectionDuration" to collectionDuration.get(),
            "collectionTimes" to collectionTimes.get(),
            "noResponseTimes" to noResponseTimes.get(),
            "delayDuration" to delayDuration.get()
        )
        isDataModified.value = false
        isInitializing = false
    }

    override fun registerField() {
        listOf(
            acquisitionFrequency,
            collectionDuration,
            collectionTimes,
            noResponseTimes,
            delayDuration
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
                "acquisitionFrequency" -> acquisitionFrequency.get() != value
                "collectionDuration" -> collectionDuration.get() != value
                "collectionTimes" -> collectionTimes.get() != value
                "noResponseTimes" -> noResponseTimes.get() != value
                "delayDuration" -> delayDuration.get() != value
                else -> false
            }
        }
    }
}