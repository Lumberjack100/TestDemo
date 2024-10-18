package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


/**
 * 创建者：gonghe
 * 创建时间：2024/10/18
 * 描述： 管理加载对话框状态
 */
class LoadingDialogViewModel : ViewModel() {
    private val _loadingState = MutableStateFlow<LoadingState>(LoadingState.Hidden)
    val loadingState: StateFlow<LoadingState> = _loadingState

    /**
     * 显示加载对话框，并设置消息。
     * @param message 要显示的消息。
     */
    fun showLoading(message: String = "加载中...") {
        _loadingState.value = LoadingState.Visible(message)
    }

    /**
     * 隐藏加载对话框。
     */
    fun hideLoading() {
        _loadingState.value = LoadingState.Hidden
    }

    /**
     * 更新加载对话框的消息。
     * @param message 新的消息。
     */
    fun updateMessage(message: String) {
        val currentState = _loadingState.value
        if (currentState is LoadingState.Visible) {
            _loadingState.value = currentState.copy(message = message)
        }
    }
}


/**
 * 表示加载对话框的状态。
 */
sealed class LoadingState {
    /**
     * 显示加载对话框，并显示指定的消息。
     */
    data class Visible(val message: String) : LoadingState()

    /**
     * 隐藏加载对话框。
     */
    object Hidden : LoadingState()
}


