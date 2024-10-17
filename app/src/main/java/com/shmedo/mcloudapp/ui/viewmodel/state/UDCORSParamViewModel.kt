package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class UDCORSParamViewModel : BaseStateViewModel() {
    val altitudeMeasureMode = NonNullObservableField("")//模式
    val domain = NonNullObservableField("")//域名
    val port = NonNullObservableField("") //端口
    val diffAccount = NonNullObservableField("")//差分账号
    val diffPassword = NonNullObservableField("")//差分密码
    val altitude = NonNullObservableField("")//海拔

    init {
        // 在所有字段初始化后调用 registerField()
        registerField()
    }

    // 设置初始状态
    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            "altitudeMeasureMode" to altitudeMeasureMode.get(),
            "domain" to domain.get(),
            "port" to port.get(),
            "diffAccount" to diffAccount.get(),
            "diffPassword" to diffPassword.get(),
            "altitude" to altitude.get()
        )
        isDataModified.value = false
        isInitializing = false
    }

    override fun registerField() {
        listOf(
            altitudeMeasureMode,
            domain,
            port,
            diffAccount,
            diffPassword,
            altitude
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
                "altitudeMeasureMode" -> altitudeMeasureMode.get() != value
                "domain" -> domain.get() != value
                "port" -> port.get() != value
                "diffAccount" -> diffAccount.get() != value
                "diffPassword" -> diffPassword.get() != value
                "altitude" -> altitude.get() != value
                else -> false
            }
        }
    }
}