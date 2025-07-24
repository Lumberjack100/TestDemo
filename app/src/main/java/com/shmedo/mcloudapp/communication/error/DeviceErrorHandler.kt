package com.shmedo.mcloudapp.communication.error

import androidx.fragment.app.Fragment
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
class DeviceErrorHandler(private val fragment: Fragment) {

    /**
     * 处理单个错误
     */
    fun handleError(error: DeviceError, config: ErrorConfig) {
        Timber.e("设备错误: ${error.message}")
        
        when (config.strategy) {
            ErrorHandlingStrategy.Silent -> {
                // 静默处理，只记录日志
                Timber.w("静默处理错误: ${error.message}")
            }
            
            ErrorHandlingStrategy.Toast -> {
                Toaster.show(error.message)
            }
            
            ErrorHandlingStrategy.Dialog -> {
                fragment.showMessageDialog(error.message)
            }
            
            ErrorHandlingStrategy.Custom -> {
                config.customHandler?.invoke(error.message)
                    ?: run {
                        Timber.w("自定义错误处理器为空，使用默认Toast处理")
                        Toaster.show(error.message)
                    }
            }
        }
    }

    /**
     * 处理多个错误
     */
    fun handleErrors(errors: List<DeviceError>, config: ErrorConfig) {
        if (errors.isEmpty()) return
        
        when (config.strategy) {
            ErrorHandlingStrategy.Silent -> {
                errors.forEach { error ->
                    Timber.w("静默处理错误: ${error.message}")
                }
            }
            
            ErrorHandlingStrategy.Toast -> {
                // 对于多个错误，只显示第一个或者汇总显示
                val message = if (errors.size == 1) {
                    errors.first().message
                } else {
                    "执行过程中发生${errors.size}个错误，第一个错误：${errors.first().message}"
                }
                Toaster.show(message)
            }
            
            ErrorHandlingStrategy.Dialog -> {
                val message = if (errors.size == 1) {
                    errors.first().message
                } else {
                    buildString {
                        appendLine("执行过程中发生以下错误：")
                        errors.forEachIndexed { index, error ->
                            appendLine("${index + 1}. ${error.message}")
                        }
                    }
                }
                fragment.showMessageDialog(message)
            }
            
            ErrorHandlingStrategy.Custom -> {
                val message = errors.joinToString("\n") { it.message }
                config.customHandler?.invoke(message)
                    ?: run {
                        Timber.w("自定义错误处理器为空，使用默认Toast处理")
                        Toaster.show(errors.first().message)
                    }
            }
        }
    }

    companion object {
        /**
         * 静默错误配置
         */
        fun silentConfig(): ErrorConfig = ErrorConfig.silent()

        /**
         * Toast错误配置
         */
        fun toastConfig(): ErrorConfig = ErrorConfig.toast()

        /**
         * Dialog错误配置
         */
        fun dialogConfig(): ErrorConfig = ErrorConfig.dialog()

        /**
         * 自定义错误配置
         */
        fun customConfig(handler: (String) -> Unit): ErrorConfig = ErrorConfig.custom(handler)
    }
} 