package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import androidx.lifecycle.MutableLiveData
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

/**
 * 创建者：gonghe
 * 创建时间：2026/1/21
 * 描述：ML101 电台配置页面的 ViewModel
 *
 * 核心业务逻辑：
 * - 工作模式分为「中心节点」和「终端节点」
 * - 中心节点模式：目标地址自动设为 0，不可编辑（广播模式）
 * - 终端节点模式：目标地址可手动输入，用于点对点通信
 *
 * 参数说明：
 * - workMode: 工作模式 (0: 中心节点, 1: 终端节点)
 * - frequency: 收发频点 (0-19 对应 470-508MHz)
 * - txPower: 发射功率 (0-20)
 * - airRate: 空中速率 (1: 2.4kbps, 2: 19.2kbps, 3: 76.8kbps)
 * - localAddress: 本机地址 (1-65535)
 * - targetAddress: 目标地址 (0-65535，中心节点自动为0)
 */
class ML101RadioConfigViewModel : BaseStateViewModel() {

    // ==================== 表单字段 ====================

    /** 工作模式显示文本（中心节点/终端节点） */
    val workMode = NonNullObservableField("")

    /** 收发频点显示文本（如：472MHz） */
    val frequency = NonNullObservableField("")

    /** 发射功率显示文本（如：20） */
    val txPower = NonNullObservableField("")

    /** 空中速率显示文本（如：19.2Kbps） */
    val airRate = NonNullObservableField("")

    /** 本机地址（用户输入，范围 1-65535） */
    val localAddress = NonNullObservableField("")

    /** 目标地址（终端节点可输入，中心节点自动为0） */
    val targetAddress = NonNullObservableField("")

    // ==================== UI 状态控制 ====================

    /**
     * 目标地址输入框是否可编辑
     * - true: 终端节点模式，可编辑
     * - false: 中心节点模式，自动设为0，不可编辑
     */
    val isTargetAddressEnabled = MutableLiveData(true)

    /**
     * 是否为中心节点模式
     * 用于 XML 布局中的双向绑定条件判断
     */
    val isCenterNode: Boolean
        get() = workMode.get() == "中心节点"

    // ==================== 生命周期方法 ====================

    init {
        // 在所有字段初始化后注册属性变化监听
        registerField()
    }

    /**
     * 保存初始状态
     * 用于检测用户是否修改了配置，控制保存按钮的启用状态
     */
    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            "workMode" to workMode.get(),
            "frequency" to frequency.get(),
            "txPower" to txPower.get(),
            "airRate" to airRate.get(),
            "localAddress" to localAddress.get(),
            "targetAddress" to targetAddress.get()
        )
        isDataModified.value = false
        isInitializing = false
    }

    /**
     * 注册表单字段的属性变化监听
     * 当任意字段值变化时，触发修改状态检测
     */
    override fun registerField() {
        listOf(
            workMode,
            frequency,
            txPower,
            airRate,
            localAddress,
            targetAddress
        ).forEach { field ->
            field.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    updateModificationStatus()
                }
            })
        }
    }

    /**
     * 检测当前表单数据是否与初始状态不同
     * 用于控制保存按钮的启用/禁用状态
     */
    override fun updateModificationStatus() {
        if (isInitializing) return
        isDataModified.value = initialState.any { (key, value) ->
            when (key) {
                "workMode" -> workMode.get() != value
                "frequency" -> frequency.get() != value
                "txPower" -> txPower.get() != value
                "airRate" -> airRate.get() != value
                "localAddress" -> localAddress.get() != value
                "targetAddress" -> targetAddress.get() != value
                else -> false
            }
        }
    }

    // ==================== 业务方法 ====================

    /**
     * 更新工作模式
     * 当切换到中心节点时，自动将目标地址设为 0 并禁用输入
     *
     * @param mode 工作模式显示文本（中心节点/终端节点）
     */
    fun updateWorkMode(mode: String) {
        workMode.set(mode)
        if (mode == "中心节点") {
            // 中心节点模式：目标地址自动设为 0，禁用输入
            targetAddress.set("0")
            isTargetAddressEnabled.value = false
        } else {
            // 终端节点模式：启用目标地址输入
            isTargetAddressEnabled.value = true
        }
    }
}
