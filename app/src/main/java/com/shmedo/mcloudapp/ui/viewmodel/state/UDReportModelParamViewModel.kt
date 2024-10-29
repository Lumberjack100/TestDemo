package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class UDReportModelParamViewModel : BaseStateViewModel() {
    val reportModel = NonNullObservableField("")//模式
    val alarmEnable = NonNullObservableField(true)//报警开启

    val firstAlarmThresholdTitle = NonNullObservableField("一级报警阈值（毫米）")
    val secondAlarmThresholdTitle = NonNullObservableField("二级报警阈值（毫米）")
    val thirdAlarmThresholdTitle = NonNullObservableField("三级报警阈值（毫米）")
    val fourthAlarmThresholdTitle = NonNullObservableField("四级报警阈值（毫米）")

    //一级报警阈值 默认40
    val firstAlarmThreshold = NonNullObservableField("40")

    //二级报警阈值 默认20
    val secondAlarmThreshold = NonNullObservableField("20")

    //三级报警阈值 默认10
    val thirdAlarmThreshold = NonNullObservableField("10")

    //四级报警阈值 默认5
    val fourthAlarmThreshold = NonNullObservableField("5")

    //上报频率
    val reportFrequency = NonNullObservableField("")


    init {
        // 在所有字段初始化后调用 registerField()
        registerField()
    }

    // 设置初始状态
    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            "reportModel" to reportModel.get(),
            "alarmEnable" to alarmEnable.get(),
            "firstAlarmThreshold" to firstAlarmThreshold.get(),
            "secondAlarmThreshold" to secondAlarmThreshold.get(),
            "thirdAlarmThreshold" to thirdAlarmThreshold.get(),
            "fourthAlarmThreshold" to fourthAlarmThreshold.get(),
            "reportFrequency" to reportFrequency.get()
        )
        isDataModified.value = false
        isInitializing = false
    }

    override fun registerField() {
        listOf(
            reportModel,
            alarmEnable,
            firstAlarmThreshold,
            secondAlarmThreshold,
            thirdAlarmThreshold,
            fourthAlarmThreshold,
            reportFrequency
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
                "reportModel" -> value != reportModel.get()
                "alarmEnable" -> value != alarmEnable.get()
                "firstAlarmThreshold" -> value != firstAlarmThreshold.get()
                "secondAlarmThreshold" -> value != secondAlarmThreshold.get()
                "thirdAlarmThreshold" -> value != thirdAlarmThreshold.get()
                "fourthAlarmThreshold" -> value != fourthAlarmThreshold.get()
                "reportFrequency" -> value != reportFrequency.get()
                else -> false
            }
        }
    }
}