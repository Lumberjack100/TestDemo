package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.databinding.Observable
import com.shmedo.mcloudapp.ui.page.base.viewmodel.BaseStateViewModel
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class M20SWorkModelViewModel : BaseStateViewModel() {

    val model = NonNullObservableField("")//模式
    val frontCalc = NonNullObservableField("")//0:关闭前端解算  1:打开前端解算  2:根据网络状态开启前端解算  默认2

    init {
        // 在所有字段初始化后调用 registerField()
        registerField()
    }

    // 设置初始状态
    override fun saveInitialState() {
        isInitializing = true
        initialState = mapOf(
            "model" to model.get(),
            "frontCalc" to frontCalc.get()
        )
        isDataModified.value = false
        isInitializing = false
    }

    override fun registerField() {
        listOf(
            model,
            frontCalc
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
                "model" -> model.get() != value
                "frontCalc" -> frontCalc.get() != value
                else -> false
            }
        }
    }

}