package com.shmedo.mcloudapp.ui.dialog

/**
 * @author：gonghe
 * @time: 2025/1/7
 * @desc: LoadingDialog配置数据类
 * 
 * 重构后的配置类，简化参数管理
 */
data class LoadingDialogConfig(
    val message: String,
    val loadingId: String = DEFAULT_LOADING_ID,
    val isCancelable: Boolean = true,
    val timeoutDuration: Long = DEFAULT_TIMEOUT,
    val onCancel: (() -> Unit)? = null
) {
    companion object {
        const val DEFAULT_TIMEOUT = 300_000L // 300秒
        const val DEFAULT_LOADING_ID = "GLOBAL_LOADING"
    }
}
