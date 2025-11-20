package com.shmedo.mcloudapp.communication.model

import com.shmedo.core.commonlib.utils.AppContants
import com.shmedo.mcloudapp.communication.session.CommandPriority

/**
 * 指令执行结果密封类
 */
sealed class CommandResult {
    /**
     * 指令执行成功
     * @param responseData 响应数据
     * @param command 原始指令
     * @param timestamp 执行时间戳
     */
    data class Success(
        val responseData: String,
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
     * TCP通信错误
     */
    data class Tcp(val errorMsg: String) : DeviceError("TCP错误: $errorMsg")

    /**
     * 指令超时错误
     */
    data class Timeout(val command: String, val timeoutMs: Long) :
        DeviceError("设备未响应")

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
    data class Validation(val errorMsg: String) : DeviceError(errorMsg)

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
    val customHandler: ((DeviceError) -> Unit)? = null
) {
    companion object {
        fun silentConfig() = ErrorConfig(strategy = ErrorHandlingStrategy.Silent)
        fun toastConfig() = ErrorConfig(strategy = ErrorHandlingStrategy.Toast)
        fun dialogConfig() = ErrorConfig(strategy = ErrorHandlingStrategy.Dialog)
        fun customConfig(handler: (DeviceError) -> Unit) = ErrorConfig(
            strategy = ErrorHandlingStrategy.Custom,
            customHandler = handler
        )
    }
}

/**
 * 指令执行配置
 */
data class CommandConfig(
    val timeout: Long = AppContants.Communication.DELAY_10000_MILLIS,//默认10秒超时
    val delayBeforeSend: Long = 0L,
    val ownerId: String? = null,  // 新增：指令发起者ID
    val priority: CommandPriority = CommandPriority.HIGH  // 新增：指令优先级
)

/**
 * 指令序列执行配置
 */
data class CommandSequenceConfig(
    val timeout: Long = AppContants.Communication.DELAY_10000_MILLIS,//默认10秒超时
    val delayBeforeSend: Long = 0L,
    val showLoadingDialog: Boolean = true,
    val loadingMessage: String = "处理中...",
    val errorConfig: ErrorConfig = ErrorConfig.toastConfig(),
    val autoFinishRefreshLayoutOnComplete: Boolean = true,//是否在执行完成后自动结束刷新动画
    val stopOnFirstCmdError: Boolean = true,// 是否在执行第一条指令下发失败时停止后续指令执行
    /**
     * 是否启用业务层指令响应内容解析失败中断功能
     * 当启用时，如果 handleCommandResponse 抛出异常，会中断后续指令执行
     */
    val enableBusinessParseFailureInterrupt: Boolean = true,
    val ownerId: String? = null,  // 新增：指令序列发起者ID
    val priority: CommandPriority = CommandPriority.HIGH  // 新增：指令序列优先级
)


/**
 * 指令序列执行回调
 */
data class CommandSequenceCallbacks(
    /**
     * 单条指令成功回调
     * @param result 指令执行结果
     * @return Boolean? 当enableBusinessParseFailureInterrupt=true时有效：
     *         - true: 继续执行后续指令
     *         - false: 中断后续指令执行
     *         - null: 使用默认行为（继续执行）
     */
    val onSuccess: ((CommandResult.Success) -> Boolean?)? = null,
    val onComplete: (List<CommandResult>) -> Unit = {},
    val onError: (DeviceError, String) -> Unit = { _, _ -> }
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
