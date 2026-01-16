package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

/**
 * @author：gonghe
 * @time: 2026/1/9
 * @desc: GT600 工作模式配置状态管理 ViewModel
 *
 * 管理工作模式配置页面的所有状态数据：
 * - 工作模式（基站/测站）
 * - 坐标初始化（是/否）
 * - 初始化模式（自动/手动）
 * - 坐标数据（经度、纬度、高度）
 */
class GT600WorkModeConfigViewModel : BaseStateViewModel() {
    
    // ==================== 状态字段 ====================
    
    /** 工作模式：基站/测站 */
    val workMode = NonNullObservableField("")
    
    /** 坐标初始化：是/否 */
    val coordinateInitialization = NonNullObservableField("")
    
    /** 初始化模式：自动/手动 */
    val initializationMode = NonNullObservableField("")
    
    /** 经度（度） */
    val longitude = NonNullObservableField("")
    
    /** 纬度（度） */
    val latitude = NonNullObservableField("")
    
    /** 高度（米） */
    val altitude = NonNullObservableField("")

    init {
        // 在所有字段初始化后调用 registerField()
        registerField()
    }

    // ==================== 状态管理方法 ====================

    /**
     * 保存初始状态，用于检测数据是否被修改
     */
    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            "workMode" to workMode.get(),
            "coordinateInitialization" to coordinateInitialization.get(),
            "initializationMode" to initializationMode.get(),
            "longitude" to longitude.get(),
            "latitude" to latitude.get(),
            "altitude" to altitude.get()
        )
        isDataModified.value = false
        isInitializing = false
    }

    /**
     * 注册字段变化监听器
     */
    override fun registerField() {
        listOf(
            workMode,
            coordinateInitialization,
            initializationMode,
            longitude,
            latitude,
            altitude
        ).forEach { field ->
            field.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    updateModificationStatus()
                }
            })
        }
    }

    /**
     * 更新数据修改状态
     */
    override fun updateModificationStatus() {
        if (isInitializing) return
        isDataModified.value = initialState.any { (key, value) ->
            when (key) {
                "workMode" -> workMode.get() != value
                "coordinateInitialization" -> coordinateInitialization.get() != value
                "initializationMode" -> initializationMode.get() != value
                "longitude" -> longitude.get() != value
                "latitude" -> latitude.get() != value
                "altitude" -> altitude.get() != value
                else -> false
            }
        }
    }
}
