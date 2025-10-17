package com.shmedo.mcloudapp.communication.error

import com.hjq.toast.Toaster
import com.shmedo.mcloudapp.communication.model.DeviceError
import com.shmedo.mcloudapp.communication.model.ErrorConfig
import com.shmedo.mcloudapp.communication.model.ErrorHandlingStrategy
import com.shmedo.mcloudapp.extensions.showMessageDialog
import timber.log.Timber

/**
 * 设备错误处理器
 * 负责根据配置统一处理各种设备错误
 */
class DeviceErrorHandler() {

    /**
     * 处理单个错误
     */
    fun handleError(error: DeviceError, config: ErrorConfig) {
        when (config.strategy) {
            ErrorHandlingStrategy.Silent -> {
                // 静默处理，只记录日志
                Timber.i("静默处理错误: ${error.message}")
            }
            
            ErrorHandlingStrategy.Toast -> {
                Timber.i("Toast 处理错误: ${error.message}")
                Toaster.show(error.message)
            }
            
            ErrorHandlingStrategy.Dialog -> {
                Timber.i("Dialog 处理错误: ${error.message}")
                showMessageDialog(error.message)
            }
            
            ErrorHandlingStrategy.Custom -> {
                Timber.i("Custom 处理错误: ${error.message}")
                config.customHandler?.invoke(error)
                    ?: run {
                        Timber.w("自定义错误处理器为空，使用默认Toast处理")
                        Toaster.show(error.message)
                    }
            }
        }
    }

} 