package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class M50CORSConfigViewModel : BaseStateViewModel() {
    val isOpened = NonNullObservableField(false) // CORS启用
    val domain = NonNullObservableField("")//域名
    val port = NonNullObservableField("") //端口
    val diffAccount = NonNullObservableField("")//差分账号
    val diffPassword = NonNullObservableField("")//差分密码

    init {
        // 在所有字段初始化后调用 registerField()
        registerField()
    }

    // 设置初始状态
    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            "isOpened" to isOpened.get(),
            "domain" to domain.get(),
            "port" to port.get(),
            "diffAccount" to diffAccount.get(),
            "diffPassword" to diffPassword.get()
        )
        isDataModified.value = false
        isInitializing = false
    }

    override fun registerField() {
        listOf(
            isOpened,
            domain,
            port,
            diffAccount,
            diffPassword
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
                "isOpened" -> isOpened.get() != value
                "domain" -> domain.get() != value
                "port" -> port.get() != value
                "diffAccount" -> diffAccount.get() != value
                "diffPassword" -> diffPassword.get() != value
                else -> false
            }
        }
    }
} 