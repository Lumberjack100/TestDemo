package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class M50SensorConfigViewModel : BaseStateViewModel() {
    val isEditable = NonNullObservableField(true)

    // GNSS配置
    val longitude = NonNullObservableField("") // 经度
    val latitude = NonNullObservableField("") // 纬度
    val altitude = NonNullObservableField("") // 高度

    // 倾角配置
    val xAxis = NonNullObservableField("") // X轴倾角值
    val yAxis = NonNullObservableField("") // Y轴倾角值
    val zAxis = NonNullObservableField("") // Z轴倾角值

    init {
        // 在所有字段初始化后调用 registerField()
        registerField()
    }

    // 设置初始状态
    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            "longitude" to longitude.get(),
            "latitude" to latitude.get(),
            "altitude" to altitude.get(),
            "xAxis" to xAxis.get(),
            "yAxis" to yAxis.get(),
            "zAxis" to zAxis.get()
        )
        isDataModified.value = false
        isInitializing = false
    }

    override fun registerField() {
        listOf(
            longitude,
            latitude,
            altitude,
            xAxis,
            yAxis,
            zAxis
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
                "longitude" -> longitude.get() != value
                "latitude" -> latitude.get() != value
                "altitude" -> altitude.get() != value
                "xAxis" -> xAxis.get() != value
                "yAxis" -> yAxis.get() != value
                "zAxis" -> zAxis.get() != value
                else -> false
            }
        }
    }
} 