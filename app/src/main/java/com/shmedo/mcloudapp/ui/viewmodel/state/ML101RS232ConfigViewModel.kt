package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

/**
 * 创建者：gonghe
 * 创建时间：2026/01/21
 * 描述：ML101 RS232 配置页面 ViewModel
 *
 * 管理 RS232 串口配置参数的状态，支持数据修改检测
 *
 * 字段说明：
 * - isOpened: 功能开关状态 (true-开启, false-关闭)
 * - functionMode: 功能选择 (关闭输出/日志输出/数据透传)
 * - baudRate: 波特率 (9600/57600/115200)
 * - dataBit: 数据位 (5/6/7/8)
 * - parityBit: 校验位 (NONE/ODD/EVEN/MARK/SPACE)
 * - stopBit: 停止位 (1/1.5/2)
 */
class ML101RS232ConfigViewModel : BaseStateViewModel() {

    // ==================== 状态字段 ====================

    /** 功能开关：true-开启, false-关闭 */
    val isOpened = NonNullObservableField(true)

    /** 功能选择：关闭输出/日志输出/数据透传 */
    val functionMode = NonNullObservableField("关闭输出")

    /** 波特率：9600/57600/115200 */
    val baudRate = NonNullObservableField("115200")

    /** 数据位：5/6/7/8 */
    val dataBit = NonNullObservableField("8")

    /** 校验位：NONE/ODD/EVEN/MARK/SPACE */
    val parityBit = NonNullObservableField("NONE")

    /** 停止位：1/1.5/2 */
    val stopBit = NonNullObservableField("1")

    // ==================== 初始化 ====================

    init {
        registerField()
    }

    // ==================== 状态管理方法 ====================

    /**
     * 保存当前状态为初始状态
     * 用于后续判断数据是否被修改
     */
    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            "isOpened" to isOpened.get(),
            "functionMode" to functionMode.get(),
            "baudRate" to baudRate.get(),
            "dataBit" to dataBit.get(),
            "parityBit" to parityBit.get(),
            "stopBit" to stopBit.get()
        )
        isDataModified.value = false
        isInitializing = false
    }

    /**
     * 注册字段变更监听器
     * 当任意字段值变化时触发数据修改状态更新
     */
    override fun registerField() {
        listOf(
            isOpened,
            functionMode,
            baudRate,
            dataBit,
            parityBit,
            stopBit
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
     * 比较当前值与初始值，判断数据是否被修改
     */
    override fun updateModificationStatus() {
        if (isInitializing) return
        isDataModified.value = initialState.any { (key, value) ->
            when (key) {
                "isOpened" -> isOpened.get() != value
                "functionMode" -> functionMode.get() != value
                "baudRate" -> baudRate.get() != value
                "dataBit" -> dataBit.get() != value
                "parityBit" -> parityBit.get() != value
                "stopBit" -> stopBit.get() != value
                else -> false
            }
        }
    }
}
