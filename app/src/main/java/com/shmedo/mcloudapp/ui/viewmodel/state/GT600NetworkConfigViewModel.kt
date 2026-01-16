package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

/**
 * @author：gonghe
 * @time: 2026/1/9
 * @desc: GT600 网络配置页面 ViewModel
 *
 * 包含两个配置分组的数据字段:
 * 1. 移动网络分组:
 *    - 4G通信开关
 *    - APN
 *    - 用户名
 *    - 密码
 * 2. 以太网络分组:
 *    - IP分配方式
 *    - IP地址
 *    - 网关
 *    - 首选DNS
 */
class GT600NetworkConfigViewModel : BaseStateViewModel() {

    // ========== 移动网络分组参数 ==========
    /** 4G通信开关是否禁用（4G模式下禁用） */
    val is4GDisabled = NonNullObservableField(false)

    /** 4G通信开关状态 */
    val is4GEnabled = NonNullObservableField(true)

    /** APN */
    val apnName = NonNullObservableField("")

    /** 用户名 */
    val userName = NonNullObservableField("")

    /** 密码 */
    val password = NonNullObservableField("")

    // ========== 以太网络分组参数 ==========
    /** IP分配方式显示值 */
    val ipMode = NonNullObservableField("")

    /** IP地址 */
    val ipAddress = NonNullObservableField("")

    /** 网关 */
    val gateway = NonNullObservableField("")

    /** 首选DNS */
    val preferredDNS = NonNullObservableField("")

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
            "is4GEnabled" to is4GEnabled.get(),
            "apnName" to apnName.get(),
            "userName" to userName.get(),
            "password" to password.get(),
            "ipMode" to ipMode.get(),
            "ipAddress" to ipAddress.get(),
            "gateway" to gateway.get(),
            "preferredDNS" to preferredDNS.get()
        )
        isDataModified.value = false
        isInitializing = false
    }

    /**
     * 注册字段变更监听器
     */
    override fun registerField() {
        val allFields = listOf(
            is4GEnabled,
            apnName,
            userName,
            password,
            ipMode,
            ipAddress,
            gateway,
            preferredDNS
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
                "is4GEnabled" -> is4GEnabled.get() != value
                "apnName" -> apnName.get() != value
                "userName" -> userName.get() != value
                "password" -> password.get() != value
                "ipMode" -> ipMode.get() != value
                "ipAddress" -> ipAddress.get() != value
                "gateway" -> gateway.get() != value
                "preferredDNS" -> preferredDNS.get() != value
                else -> false
            }
        }
    }
}
