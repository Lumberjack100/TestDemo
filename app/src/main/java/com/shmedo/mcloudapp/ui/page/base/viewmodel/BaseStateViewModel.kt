package com.shmedo.mcloudapp.ui.page.base.viewmodel

import androidx.lifecycle.ViewModel
import com.kunminx.architecture.domain.message.MutableResult

/**
 * 创建者：gonghe
 * 创建时间：2024/10/17
 * 描述： TODO
 */
abstract class BaseStateViewModel : ViewModel() {
    var isInitializing = true
    var initialState: Map<String, Any> = emptyMap()
    val isDataModified = MutableResult<Boolean>().apply {
        value = false
    }

    //设置初始状态
    abstract fun saveInitialState()

    abstract fun registerField()

    abstract fun updateModificationStatus()
}