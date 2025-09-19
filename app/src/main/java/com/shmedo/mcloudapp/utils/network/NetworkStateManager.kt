package com.shmedo.mcloudapp.utils.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 作者　: hegaojian
 * 时间　: 2020/5/2
 * 描述　: 网络变化管理者
 */
object NetworkStateManager {

    private val _networkState = MutableStateFlow(NetState(isSuccess = false))
    val networkState: StateFlow<NetState> = _networkState.asStateFlow()

    /** 仅在变化时才发射 */
    fun updateIfChanged(newState: NetState) {
        val old = _networkState.value
        // 去重：仅在变化时才发
        if (old.isSuccess == newState.isSuccess) return

        _networkState.value = newState
    }
}