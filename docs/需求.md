# BaseIOTDeviceFragment 优化方案

## 概述

完全实现 BaseIOTDeviceFragment 原有的功能逻辑，包括蓝牙连接、指令发送、指令响应、指令超时、指令错误处理等所有功能，同时解决原有代码中的职责混乱、错误处理分散、状态管理复杂等问题，提供更清晰、更可维护的架构。

## 优化点

### 1. 架构层面优化
- **职责分离**：引入通信管理器，将通信逻辑从Fragment中分离
- **策略模式**：使用通信策略抽象4G和蓝牙通信
- **统一错误处理**：集中处理各种错误类型，支持多种显示策略
- **状态管理**：使用状态机模式管理设备和执行状态

### 2. 业务逻辑优化
- **响应驱动**：严格按照"发送→等待响应→发送下一条"的业务逻辑
- **配置驱动**：通过配置对象控制行为，提高灵活性

### 3. 代码质量优化
- **类型安全**：使用密封类表示状态和结果
- **消除魔数**：使用常量和配置对象
- **完善日志**：提供详细的调试信息
- **异常安全**：全面的异常处理和恢复机制


## 核心组件

### 1. 通信模型 (CommandModels.kt)
```kotlin
// 指令执行结果
sealed class CommandResult {
    data class Success(val data: String, val command: String, val timestamp: Long) : CommandResult()
    data class Error(val error: DeviceError, val command: String) : CommandResult()
    data class Timeout(val command: String, val timeoutMs: Long) : CommandResult()
}

// 设备错误类型
sealed class DeviceError(val message: String, val cause: Throwable? = null) {
    data class Network(val errorMsg: String) : DeviceError("网络错误: $errorMsg")
    data class Bluetooth(val errorMsg: String) : DeviceError("蓝牙错误: $errorMsg")
    data class Timeout(val command: String, val timeoutMs: Long) : DeviceError("响应超时: $command")
    // ... 其他错误类型
}
```

### 2. 通信策略 (CommunicationStrategy.kt)
```kotlin
interface CommunicationStrategy {
    suspend fun sendCommand(command: String, config: CommandConfig = CommandConfig())
    fun isConnected(): Boolean
    fun getConnectionState(): Flow<ConnectionState>
}

// 4G网络通信策略
class NetCommunicationStrategy(
    private val netViewModel: NetIOTCommandViewModel,
    private val deviceInfo: DeviceInfo
) : CommunicationStrategy

// 蓝牙通信策略
class BleCommunicationStrategy(
    private val bleViewModel: BleViewModel,
    private val deviceInfo: DeviceInfo
) : CommunicationStrategy
```

### 3. 响应驱动指令执行器 (ResponseDrivenCommandExecutor.kt)
```kotlin
class ResponseDrivenCommandExecutor {
    fun executeSequence(
        commands: List<String>,
        config: CommandSequenceConfig,
        onComplete: (List<CommandResult>) -> Unit,
        onError: (DeviceError, String) -> Unit
    )
    
    fun handleResponse(response: String)
    fun handleError(error: DeviceError, command: String)
    fun handleTimeout(command: String, timeoutMs: Long)
}
```

### 4. 设备通信管理器 (DeviceCommunicationManager.kt)
```kotlin
class DeviceCommunicationManager {
    fun executeCommandSequence(
        commands: List<String>,
        config: CommandSequenceConfig,
        onComplete: (List<CommandResult>) -> Unit,
        onError: (DeviceError, String) -> Unit
    )
    
    fun handleCommandResponse(response: String)
    fun handleCommandError(error: DeviceError, command: String)
    fun handleCommandTimeout(command: String, timeoutMs: Long)
}
```

### 5. 错误处理器 (DeviceErrorHandler.kt)
```kotlin
class DeviceErrorHandler {
    fun handleError(error: DeviceError, config: ErrorConfig)
    fun handleErrors(errors: List<DeviceError>, config: ErrorConfig)
    
    companion object {
        fun silentConfig(): ErrorConfig
        fun toastConfig(): ErrorConfig
        fun dialogConfig(): ErrorConfig
        fun customConfig(handler: (String) -> Unit): ErrorConfig
    }
}
```

## 新API使用方法

### 1. 基本用法 - 发送指令序列

```kotlin
class MyDeviceFragment : OptimizedBaseIOTDeviceFragment() {
    
    private fun queryDeviceInfo() {
        val commands = listOf(
            IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DEVICE_STATUS),
            IOTCommandUtil.getCommand(IOTCommandType.QUERY_DEVICE_STATUS)
        )
        
        sendCommandSequence(
            commands = commands,
            config = CommandSequenceConfig(
                timeout = 15_000L,
                delayBeforeSend = 100L,
                errorHandling = ErrorConfig(
                    strategy = ErrorHandlingStrategy.Dialog
                )
            ),
            onComplete = {
                binding.refreshLayout.finish()
                Timber.i("查询完成")
            }
        )
    }
}
```

### 2. 高级用法 - 配置化行为

```kotlin
private fun performComplexOperation() {
    val commands = buildCommandList()
    
    sendCommandSequence(
        commands = commands,
        config = CommandSequenceConfig(
            timeout = 20_000L,
            delayBeforeSend = 200L,
            stopOnFirstError = false, // 出错时继续执行
            errorHandling = ErrorConfig(
                strategy = ErrorHandlingStrategy.Custom { errorMsg ->
                    showCustomErrorDialog(errorMsg)
                },
                shouldDismissLoading = false
            )
        ),
        onComplete = { results ->
            processResults(results)
        },
        onError = { error, command ->
            handleCriticalError(error, command)
        }
    )
}
```

### 3. 单条指令便捷方法

```kotlin
private fun sendSingleQuery() {
    sendSingleCommand(
        command = IOTCommandUtil.getCommand(IOTCommandType.MD_GET_DEVICE_STATUS),
        config = CommandSequenceConfig(
            timeout = 10_000L,
            errorHandling = ErrorConfig(strategy = ErrorHandlingStrategy.Toast)
        ),
        onSuccess = { response ->
            updateUI(response)
        }
    )
}
```

### 4. 处理指令响应

```kotlin
override fun handleCommandResponse(cmdStr: String) {
    when (IOTCommandUtil.extractCommandType(cmdStr)) {
        IOTCommandType.MD_GET_DEVICE_STATUS -> {
            val result = iotParseManager.parse<DeviceStatus>(cmdStr, IOTCommandType.MD_GET_DEVICE_STATUS)
            when (result) {
                is IOTCommandResult.Success -> {
                    updateDeviceStatus(result.data)
                }
                is IOTCommandResult.Failure -> {
                    Timber.e("解析设备状态失败: ${result.message}")
                }
            }
        }
        // 处理其他指令类型...
    }
}
```


## 配置参考

### ErrorHandlingStrategy 选择指南

| 场景 | 推荐策略 | 说明 |
|------|----------|------|
| 查询操作失败 | `ErrorHandlingStrategy.Toast` | 用户能看到但不会打断流程 |
| 保存操作失败 | `ErrorHandlingStrategy.Dialog` | 需要用户确认的重要错误 |
| 批量操作中的单个失败 | `ErrorHandlingStrategy.Silent` | 静默处理，在最后统一报告 |
| 需要特殊处理的错误 | `ErrorHandlingStrategy.Custom` | 自定义处理逻辑 |

### CommandSequenceConfig 配置指南

| 参数 | 推荐值 | 说明 |
|------|--------|------|
| `timeout` | 10_000L | 大部分指令10秒足够 |
| `delayBeforeSend` | 100L | 给设备一些缓冲时间 |
| `stopOnFirstError` | true | 大部分情况下出错就停止 |
| `errorHandling` | 根据场景选择 | 参考上表 |

## 性能优化建议

1. **合理设置超时时间**
   - 查询操作：10-15秒
   - 配置操作：15-20秒

2. **错误处理策略**
   - 频繁操作使用 Silent 或 Toast
   - 重要操作使用 Dialog
   - 批量操作使用 Custom

## 调试和日志

### 启用详细日志
```kotlin
// 在 Application 中设置
if (BuildConfig.DEBUG) {
    Timber.plant(Timber.DebugTree())
}
```

### 查看通信日志
```kotlin
// 通信管理器会自动记录详细的指令执行日志
// 可以通过 Timber 查看执行流程
```