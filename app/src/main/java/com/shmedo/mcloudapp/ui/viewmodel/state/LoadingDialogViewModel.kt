package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap


/**
 * 创建者：gonghe
 * 创建时间：2024/10/18
 * 描述： 管理加载对话框状态
 */
class LoadingDialogViewModel : ViewModel() {
    private val _dialogState = MutableStateFlow<LoadingDialogState>(LoadingDialogState.Hidden)
    val dialogState = _dialogState.asStateFlow()

    private val timeoutJobs = ConcurrentHashMap<String, Job>()
    private val stateUpdateLock = Any()

    fun showLoading(config: LoadingConfig) {
        synchronized(stateUpdateLock) {
            val state = LoadingDialogState.Visible(
                message = config.message,
                loadingId = config.loadingId,
                isCancelable = config.isCancelable,
                timeoutDuration = config.timeoutDuration,
                onCancel = config.onCancel
            )
            _dialogState.value = state
            setupTimeout(config.loadingId, config.timeoutDuration)
        }
    }

    private fun setupTimeout(loadingId: String, timeoutDuration: Long) {
        timeoutJobs[loadingId]?.cancel()
        if (timeoutDuration > 0) {
            timeoutJobs[loadingId] = viewModelScope.launch {
                try {
                    delay(timeoutDuration)
                    hideLoading(loadingId)
                } catch (e: Exception) {
                    // 处理超时任务异常
                }
            }
        }
    }

    private fun hideLoading(loadingId: String = LoadingDialogState.GLOBAL_LOADING) {
        synchronized(stateUpdateLock) {
            timeoutJobs[loadingId]?.cancel()
            timeoutJobs.remove(loadingId)

            val currentState = _dialogState.value
            if (currentState is LoadingDialogState.Visible && currentState.loadingId == loadingId) {
                _dialogState.value = LoadingDialogState.Hidden
            }
        }
    }

    fun updateMessage(loadingId: String, message: String) {
        synchronized(stateUpdateLock) {
            val currentState = _dialogState.value
            if (currentState is LoadingDialogState.Visible && currentState.loadingId == loadingId) {
                _dialogState.value = currentState.copy(message = message)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        clearTimeoutJobs()
    }

    fun clearTimeoutJobs() {
        synchronized(stateUpdateLock) {
            timeoutJobs.values.forEach { it.cancel() }
            timeoutJobs.clear()
        }
    }
}

sealed class LoadingDialogState {
    data class Visible(
        val message: String,
        val loadingId: String,
        val isCancelable: Boolean = true,
        val timeoutDuration: Long = DEFAULT_TIMEOUT,
        val onCancel: (() -> Unit)? = null
    ) : LoadingDialogState()

    data object Hidden : LoadingDialogState()

    companion object {
        const val DEFAULT_TIMEOUT = 300_000L // 300 seconds
        const val GLOBAL_LOADING = "GLOBAL_LOADING"
    }
}

data class LoadingConfig(
    val message: String,
    val loadingId: String = LoadingDialogState.GLOBAL_LOADING,
    val isCancelable: Boolean = true,
    val timeoutDuration: Long = LoadingDialogState.DEFAULT_TIMEOUT,
    val onCancel: (() -> Unit)? = null
)