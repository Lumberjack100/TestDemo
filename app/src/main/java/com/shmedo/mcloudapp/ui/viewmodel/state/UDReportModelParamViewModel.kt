package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class UDReportModelParamViewModel : BaseStateViewModel() {
    val reportModel = NonNullObservableField("")//上报模式
    val alarmModel = NonNullObservableField("")//报警模式

    val firstAlarmThresholdTitle = NonNullObservableField("一级报警阈值（毫米）")
    val secondAlarmThresholdTitle = NonNullObservableField("二级报警阈值（毫米）")
    val thirdAlarmThresholdTitle = NonNullObservableField("三级报警阈值（毫米）")
    val fourthAlarmThresholdTitle = NonNullObservableField("四级报警阈值（毫米）")

    val firstAlarmThreshold = NonNullObservableField("40")//一级报警阈值 默认40
    val secondAlarmThreshold = NonNullObservableField("20") //二级报警阈值 默认20
    val thirdAlarmThreshold = NonNullObservableField("10")//三级报警阈值 默认10
    val fourthAlarmThreshold = NonNullObservableField("5")//四级报警阈值 默认5

    val addReportThreshold = NonNullObservableField("")//加报阈值

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
            "alarmModel" to alarmModel.get(),
            "firstAlarmThreshold" to firstAlarmThreshold.get(),
            "secondAlarmThreshold" to secondAlarmThreshold.get(),
            "thirdAlarmThreshold" to thirdAlarmThreshold.get(),
            "fourthAlarmThreshold" to fourthAlarmThreshold.get(),
            "addReportThreshold" to addReportThreshold.get(),
            "reportFrequency" to reportFrequency.get()
        )
        isDataModified.value = false
        isInitializing = false
    }

    override fun registerField() {
        listOf(
            reportModel,
            alarmModel,
            firstAlarmThreshold,
            secondAlarmThreshold,
            thirdAlarmThreshold,
            fourthAlarmThreshold,
            addReportThreshold,
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
                "alarmModel" -> value != alarmModel.get()
                "firstAlarmThreshold" -> value != firstAlarmThreshold.get()
                "secondAlarmThreshold" -> value != secondAlarmThreshold.get()
                "thirdAlarmThreshold" -> value != thirdAlarmThreshold.get()
                "fourthAlarmThreshold" -> value != fourthAlarmThreshold.get()
                "addReportThreshold" -> value != addReportThreshold.get()
                "reportFrequency" -> value != reportFrequency.get()
                else -> false
            }
        }
    }
}