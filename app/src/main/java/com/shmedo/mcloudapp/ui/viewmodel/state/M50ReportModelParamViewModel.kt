package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class M50ReportModelParamViewModel : BaseStateViewModel() {
    val reportModel = NonNullObservableField("")//上报模式
    val workModel = NonNullObservableField("")//工作模式
    val networkModel = NonNullObservableField("")//网络模式
    val memsThreshold = NonNullObservableField("")//MEMS 触发阈值
    val alarmEnable = NonNullObservableField(true)//四级报警启用
    val firstAlarmThresholdTitle = NonNullObservableField("一级报警阈值（毫米）")
    val secondAlarmThresholdTitle = NonNullObservableField("二级报警阈值（毫米）")
    val thirdAlarmThresholdTitle = NonNullObservableField("三级报警阈值（毫米）")
    val fourthAlarmThresholdTitle = NonNullObservableField("四级报警阈值（毫米）")

    val firstAlarmThreshold = NonNullObservableField("")//一级报警阈值 默认40
    val secondAlarmThreshold = NonNullObservableField("")//二级报警阈值 默认20
    val thirdAlarmThreshold = NonNullObservableField("")//三级报警阈值 默认10
    val fourthAlarmThreshold = NonNullObservableField("")//四级报警阈值 默认5
    
    init {
        // 在所有字段初始化后调用 registerField()
        registerField()
    }
    
    // 设置初始状态
    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            "reportModel" to reportModel.get(),
            "workModel" to workModel.get(),
            "networkModel" to networkModel.get(),
            "memsThreshold" to memsThreshold.get(),
            "alarmEnable" to alarmEnable.get(),
            "firstAlarmThreshold" to firstAlarmThreshold.get(),
            "secondAlarmThreshold" to secondAlarmThreshold.get(),
            "thirdAlarmThreshold" to thirdAlarmThreshold.get(),
            "fourthAlarmThreshold" to fourthAlarmThreshold.get()
        )
        isDataModified.value = false
        isInitializing = false
    }

    override fun registerField() {
        listOf(
            reportModel,
            workModel,
            networkModel,
            memsThreshold,
            firstAlarmThreshold,
            secondAlarmThreshold,
            thirdAlarmThreshold,
            fourthAlarmThreshold
        ).forEach { field ->
            field.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    updateModificationStatus()
                }
            })
        }
        
        // 为 alarmEnable 单独添加监听器，因为它是布尔类型
        alarmEnable.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                updateModificationStatus()
            }
        })
    }

    override fun updateModificationStatus() {
        if (isInitializing) return
        isDataModified.value = initialState.any { (key, value) ->
            when (key) {
                "reportModel" -> reportModel.get() != value
                "workModel" -> workModel.get() != value
                "networkModel" -> networkModel.get() != value
                "memsThreshold" -> memsThreshold.get() != value
                "alarmEnable" -> alarmEnable.get() != value
                "firstAlarmThreshold" -> firstAlarmThreshold.get() != value
                "secondAlarmThreshold" -> secondAlarmThreshold.get() != value
                "thirdAlarmThreshold" -> thirdAlarmThreshold.get() != value
                "fourthAlarmThreshold" -> fourthAlarmThreshold.get() != value
                else -> false
            }
        }
    }
}