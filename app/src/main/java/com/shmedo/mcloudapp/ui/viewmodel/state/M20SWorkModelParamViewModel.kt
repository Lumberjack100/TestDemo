package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class M20SWorkModelParamViewModel : BaseStateViewModel() {
    val isRadioEnable = NonNullObservableField(true)//电台是否启用

    // M20S版本移除上报模式和网络模式
    val workModel = NonNullObservableField("")//工作模式
    
    // 新增前端解算字段
    val frontendCalculation = NonNullObservableField("")//前端解算
    
    // 坐标初始化相关字段
    val coordinateInitialization = NonNullObservableField("")//坐标初始化
    val initializationMode = NonNullObservableField("")//初始化模式
    val longitude = NonNullObservableField("")//经度（度）
    val latitude = NonNullObservableField("")//纬度（度）
    val altitude = NonNullObservableField("")//高度（米）
    val initializationTime = NonNullObservableField("")//初始化时间
    val calculationIntervalTime = NonNullObservableField("")//解算间隔时间

    init {
        // 在所有字段初始化后调用 registerField()
        registerField()
    }
    
    // 设置初始状态
    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            "workModel" to workModel.get(),
            "frontendCalculation" to frontendCalculation.get(),
            "coordinateInitialization" to coordinateInitialization.get(),
            "initializationMode" to initializationMode.get(),
            "longitude" to longitude.get(),
            "latitude" to latitude.get(),
            "altitude" to altitude.get(),
            "initializationTime" to initializationTime.get(),
            "calculationIntervalTime" to calculationIntervalTime.get()
        )
        isDataModified.value = false
        isInitializing = false
    }

    override fun registerField() {
        listOf(
            workModel,
            frontendCalculation,
            coordinateInitialization,
            initializationMode,
            longitude,
            latitude,
            altitude,
            initializationTime,
            calculationIntervalTime
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
                "workModel" -> workModel.get() != value
                "frontendCalculation" -> frontendCalculation.get() != value
                "coordinateInitialization" -> coordinateInitialization.get() != value
                "initializationMode" -> initializationMode.get() != value
                "longitude" -> longitude.get() != value
                "latitude" -> latitude.get() != value
                "altitude" -> altitude.get() != value
                "initializationTime" -> initializationTime.get() != value
                "calculationIntervalTime" -> calculationIntervalTime.get() != value
                else -> false
            }
        }
    }
}