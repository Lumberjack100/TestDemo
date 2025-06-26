package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class MR702RS485Port2ViewModel : BaseStateViewModel() {
    val acquisitionFrequency = NonNullObservableField("")//采集频率
    val collectionTimes = NonNullObservableField("")//采集次数
    val noResponseTimes = NonNullObservableField("")//无应答次数
    val delayDuration = NonNullObservableField("")//延时时间

    val collectorType = NonNullObservableField("")//采集器类型
    val collectorAddress = NonNullObservableField("")//采集器地址
    val baudRate = NonNullObservableField("")//波特率
    val dataBit = NonNullObservableField("5")//数据位
    val checkBit = NonNullObservableField("NONE")//校验位
    val stopBit = NonNullObservableField("1")//停止位

    init {
        // 在所有字段初始化后调用 registerField()
        registerField()
    }

    // 设置初始状态
    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            "acquisitionFrequency" to acquisitionFrequency.get(),
            "collectionTimes" to collectionTimes.get(),
            "noResponseTimes" to noResponseTimes.get(),
            "delayDuration" to delayDuration.get(),
            "collectorType" to collectorType.get(),
            "collectorAddress" to collectorAddress.get(),
            "baudRate" to baudRate.get(),
            "dataBit" to dataBit.get(),
            "checkBit" to checkBit.get(),
            "stopBit" to stopBit.get()
        )
        isDataModified.value = false
        isInitializing = false
    }

    override fun registerField() {
        listOf(
            acquisitionFrequency,
            collectionTimes,
            noResponseTimes,
            delayDuration,
            collectorType,
            collectorAddress,
            baudRate,
            dataBit,
            checkBit,
            stopBit
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
                "collectionTimes" -> collectionTimes.get() != value
                "noResponseTimes" -> noResponseTimes.get() != value
                "delayDuration" -> delayDuration.get() != value
                "collectorType" -> collectorType.get() != value
                "collectorAddress" -> collectorAddress.get() != value
                "baudRate" -> baudRate.get() != value
                "dataBit" -> dataBit.get() != value
                "checkBit" -> checkBit.get() != value
                "stopBit" -> stopBit.get() != value
                else -> false
            }
        }
    }
}