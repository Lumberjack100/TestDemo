package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

/**
 * @author：gonghe
 * @time: 2026/1/9
 * @desc: GT600 串口配置页面 ViewModel
 *
 * 包含串口参数分组的数据字段:
 * - 波特率
 * - 功能选择
 * - 功能开关（暂无对应指令字段，仅展示逻辑）
 */
class GT600SerialConfigViewModel : BaseStateViewModel() {

    // ========== 串口参数 ==========
    /** 波特率显示值 */
    val baudRate = NonNullObservableField("")

    /** 功能选择显示值 */
    val functionType = NonNullObservableField("")

    /** 功能开关状态显示值（暂无对应指令字段，仅展示逻辑） */
    val functionSwitch = NonNullObservableField("")

    /** 功能开关是否可见（仅当功能选择为"传感器采集"时显示） */
    val isFunctionSwitchVisible = NonNullObservableField(true)

    init {
        // 在所有字段初始化后调用 registerField()
        registerField()
    }

    /**
     * 保存初始状态，用于检测数据是否被修改
     */
    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            "baudRate" to baudRate.get(),
            "functionType" to functionType.get(),
            "functionSwitch" to functionSwitch.get()
        )
        isDataModified.value = false
        isInitializing = false
    }

    /**
     * 注册字段变更监听器
     */
    override fun registerField() {
        val allFields = listOf(
            baudRate,
            functionType,
            functionSwitch
        )

        allFields.forEach { field ->
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
                "baudRate" -> baudRate.get() != value
                "functionType" -> functionType.get() != value
                "functionSwitch" -> functionSwitch.get() != value
                else -> false
            }
        }
    }
}
