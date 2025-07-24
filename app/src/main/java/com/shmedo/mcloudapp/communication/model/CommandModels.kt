package com.shmedo.mcloudapp.communication.model

import com.shmedo.core.commonlib.utils.AppContants

/**
 * 指令执行结果密封类
 */
sealed class CommandResult {
    /**
     * 指令执行成功
     * @param data 响应数据
     * @param command 原始指令
     * @param timestamp 执行时间戳
     */
    data class Success(
        val data: String,
        val command: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : CommandResult()

    /**
     * 指令执行错误
     * @param error 错误详情
     * @param command 原始指令
     */
    data class Error(
        val error: DeviceError,
        val command: String
    ) : CommandResult()

    /**
     * 指令执行超时
     * @param command 原始指令
     * @param timeoutMs 超时时长(毫秒)
     */
    data class Timeout(
        val command: String,
        val timeoutMs: Long
    ) : CommandResult()
}

/**
 * 设备错误类型密封类
 */
sealed class DeviceError(open val message: String, open val cause: Throwable? = null) {
    /**
     * 网络通信错误
     */
    data class Network(val errorMsg: String) : DeviceError("网络错误: $errorMsg")

    /**
     * 蓝牙通信错误
     */
    data class Bluetooth(val errorMsg: String) : DeviceError("蓝牙错误: $errorMsg")

    /**
     * 指令超时错误
     */
    data class Timeout(val command: String, val timeoutMs: Long) :
        DeviceError("指令超时: $command (${timeoutMs}ms)")

    /**
     * 设备解析错误
     */
    data class Parse(val errorMsg: String, val rawData: String = "") :
        DeviceError("解析错误: $errorMsg")

    /**
     * 设备未连接错误
     */
    data class Disconnected(val communicationType: String) :
        DeviceError("设备未连接: $communicationType")

    /**
     * 指令验证错误
     */
    data class Validation(val errorMsg: String) : DeviceError("指令验证错误: $errorMsg")

    /**
     * 未知错误
     */
    data class Unknown(override val message: String, override val cause: Throwable? = null) :
        DeviceError(message, cause)
}

/**
 * 错误处理策略
 */
enum class ErrorHandlingStrategy {
    /** 静默处理，不显示任何提示 */
    Silent,

    /** 显示Toast提示 */
    Toast,

    /** 显示Dialog提示 */
    Dialog,

    /** 自定义处理 */
    Custom
}

/**
 * 错误处理配置
 */
data class ErrorConfig(
    val strategy: ErrorHandlingStrategy = ErrorHandlingStrategy.Toast,
    val shouldDismissLoading: Boolean = true,
    val customHandler: ((String) -> Unit)? = null
) {
    companion object {
        fun silent() = ErrorConfig(strategy = ErrorHandlingStrategy.Silent)
        fun toast() = ErrorConfig(strategy = ErrorHandlingStrategy.Toast)
        fun dialog() = ErrorConfig(strategy = ErrorHandlingStrategy.Dialog)
        fun custom(handler: (String) -> Unit) = ErrorConfig(
            strategy = ErrorHandlingStrategy.Custom,
            customHandler = handler
        )
    }
}

/**
 * 指令执行配置
 */
data class CommandConfig(
    val timeout: Long = 10_000L,
    val delayBeforeSend: Long = 0L
)

/**
 * 指令序列执行配置
 */
data class CommandSequenceConfig(
    val timeout: Long = AppContants.Communication.DELAY_10000_MILLIS,//默认10秒超时
    val delayBeforeSend: Long = 0L,
    val stopOnFirstError: Boolean = true,
    val showLoadingDialog: Boolean = true,
    val loadingMessage: String = "处理中...",
    val errorHandling: ErrorConfig = ErrorConfig.toast()
)

/**
 * 设备连接状态
 */
sealed class DeviceConnectionState {
    object Disconnected : DeviceConnectionState()
    object Connecting : DeviceConnectionState()
    object Connected : DeviceConnectionState()
    data class Error(val error: DeviceError) : DeviceConnectionState()
}

/**
 * 通信执行状态
 */
sealed class CommunicationState {
    object Idle : CommunicationState()
    data class Executing(val currentCommand: String, val remainingCount: Int) : CommunicationState()
    data class Completed(val results: List<CommandResult>) : CommunicationState()
    data class Failed(val error: DeviceError, val partialResults: List<CommandResult>) :
        CommunicationState()
}

/**
 * 指令队列项
 */
internal data class CommandItem(
    val command: String,
    val config: CommandConfig,
    val timestamp: Long = System.currentTimeMillis()
) {
    val isExpired: Boolean
        get() = System.currentTimeMillis() - timestamp > config.timeout
} 