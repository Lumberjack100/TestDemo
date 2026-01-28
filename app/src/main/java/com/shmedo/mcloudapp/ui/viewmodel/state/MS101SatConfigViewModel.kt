package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import androidx.lifecycle.MutableLiveData
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

/**
 * 创建者：gonghe
 * 创建时间：2026/1/21
 * 描述：MS101 卫通配置页面的 ViewModel
 *
 * 核心业务逻辑：
 * 1. 卫星联网状态上报：关闭/开启/开启并上报数据帧号
 * 2. 卫星休眠模式：不休眠/定时休眠/自动休眠
 * 3. 卫星休眠模式等级：1-9（仅在休眠模式非"不休眠"时有效）
 * 4. 数据存储溢出处理：停止接收/循环覆盖
 * 5. 待发数据处理：不删除/全部删除/指定删除
 *    - 选择"指定删除"时，显示删除数据帧号输入框
 *
 * 参数映射：
 * - cregmode: 0-关闭, 1-开启, 2-开启并上报数据帧号
 * - cpsmmode: 0-不休眠, 1-定时休眠, 2-自动休眠
 * - cpsmlevel: 1-9
 * - svmdmode: 0-停止接收, 1-循环覆盖
 * - cclrmode: -1-全部删除, 0-不删除, 1~480-指定删除帧号
 */
class MS101SatConfigViewModel : BaseStateViewModel() {

    // ==================== 表单字段 ====================

    /** 卫星联网状态上报显示文本（关闭/开启/开启并上报数据帧号） */
    val cregMode = NonNullObservableField("")

    /** 卫星休眠模式显示文本（不休眠/定时休眠/自动休眠） */
    val cpsmMode = NonNullObservableField("")

    /** 卫星休眠模式等级显示文本（1-9） */
    val cpsmLevel = NonNullObservableField("")

    /** 数据存储溢出处理显示文本（停止接收/循环覆盖） */
    val svmdMode = NonNullObservableField("")

    /** 待发数据处理显示文本（不删除/全部删除/指定删除） */
    val cclrMode = NonNullObservableField("")

    /** 删除数据帧号（用户输入，范围 1-480，仅"指定删除"时有效） */
    val deleteFrameNo = NonNullObservableField("")

    // ==================== UI 状态控制 ====================

    /**
     * 删除数据帧号输入框是否可见
     * - true: 选择了"指定删除"，显示输入框
     * - false: 选择了"不删除"或"全部删除"，隐藏输入框
     */
    val isDeleteFrameNoVisible = MutableLiveData(false)

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
            "cregMode" to cregMode.get(),
            "cpsmMode" to cpsmMode.get(),
            "cpsmLevel" to cpsmLevel.get(),
            "svmdMode" to svmdMode.get(),
            "cclrMode" to cclrMode.get(),
            "deleteFrameNo" to deleteFrameNo.get()
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
            cregMode,
            cpsmMode,
            cpsmLevel,
            svmdMode,
            cclrMode,
            deleteFrameNo
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
                "cregMode" -> cregMode.get() != value
                "cpsmMode" -> cpsmMode.get() != value
                "cpsmLevel" -> cpsmLevel.get() != value
                "svmdMode" -> svmdMode.get() != value
                "cclrMode" -> cclrMode.get() != value
                "deleteFrameNo" -> deleteFrameNo.get() != value
                else -> false
            }
        }
    }

    // ==================== 业务方法 ====================

    /**
     * 更新待发数据处理模式
     * 当切换到"指定删除"时，显示删除数据帧号输入框；否则隐藏
     *
     * @param mode 待发数据处理模式显示文本
     */
    fun updateCclrMode(mode: String) {
        cclrMode.set(mode)
        // "指定删除"时显示删除帧号输入框
        isDeleteFrameNoVisible.value = (mode == "指定删除")
    }
}
