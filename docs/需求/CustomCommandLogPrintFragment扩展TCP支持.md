# CustomCommandLogPrintFragment 扩展 TCP 通信支持

## 修改概述

**日期**: 2024-11-18  
**作者**: Claude  
**文件**: `app/src/main/java/com/shmedo/mcloudapp/ui/page/device/common/CustomCommandLogPrintFragment.kt`

## 功能描述

将 `CustomCommandLogPrintFragment` 从仅支持蓝牙通信扩展为同时支持蓝牙(BLE)和TCP两种通信方式的实时指令调试功能。

## 主要修改内容

### 1. 新增导入

```kotlin
import com.shmedo.lib.tcp.TcpConnectClosed
import com.shmedo.lib.tcp.TcpConnectedResult
import com.shmedo.lib.tcp.TcpConnectError
import com.shmedo.lib.tcp.TcpSuccessDataResult
import com.shmedo.lib.tcp.TcpSuccessRawDataResult
```

### 2. 重构通信观察者设置

#### 新增 `setupCommunicationObserver()` 方法

根据通信方式自动选择相应的观察者：
- `BleConnect` → 使用蓝牙通信观察者
- `TcpConnect` → 使用TCP通信观察者  
- `NetPlatformConnect` → 不需要监听实时响应

```kotlin
private fun setupCommunicationObserver() {
    when (communicateWay) {
        is BleConnect -> setupBleCommunicationObserver()
        is TcpConnect -> setupTcpCommunicationObserver()
        else -> {
            Timber.d("当前通信方式不需要监听实时响应")
        }
    }
}
```

#### 新增 `setupTcpCommunicationObserver()` 方法

处理TCP通信的各种状态和数据：
- **TcpSuccessDataResult**: 接收到普通数据，显示为接收数据
- **TcpSuccessRawDataResult**: 接收到原始字节数据，转换为字符串后显示
- **TcpConnectedResult**: TCP连接成功，显示绿色提示
- **TcpConnectClosed**: TCP连接断开，显示红色警告
- **TcpConnectError**: TCP连接异常，显示红色错误

### 3. 更新类注释

将类描述从"蓝牙通讯下自定义指令调试打印输出"更新为"自定义指令调试打印输出，支持蓝牙(BLE)和TCP两种通信方式的实时指令调试"。

## 技术实现

### 数据流架构

```
TcpViewModel.data (SharedFlow)
    ↓
setupTcpCommunicationObserver()
    ↓
mStates.addLog() → UI日志列表
```

### 错误处理

- 所有观察者都使用 `try-catch` 包裹
- 异常通过 Timber 记录详细日志
- 保持应用稳定性，不会因通信异常崩溃

### 颜色标识

| 数据类型 | 颜色 | 说明 |
|---------|------|------|
| 发送数据 | `send_data_color` | 蓝色 |
| 接收数据 | `receive_data_color` | 绿色 |
| 连接成功 | `online_colorPrimary` | 深绿 |
| 连接断开/异常 | `error_FF4400` | 红色 |
| 标题文本 | `title_text_color` | 默认 |

## 兼容性说明

### 向后兼容

✅ **完全兼容**：原有的蓝牙通信功能保持不变，不会影响现有功能。

### 前向扩展

✅ **易于扩展**：如果未来需要支持其他通信方式，只需：
1. 在 `setupCommunicationObserver()` 中添加新的分支
2. 实现对应的观察者方法

## 使用方式

### 通过蓝牙连接使用

```kotlin
val bundle = CustomCommandLogPrintFragment.newBundleArguments(
    isIotCmd = true,
    type = ProductType.GT600,
    communicateWay = BleConnect,
    deviceInfo = deviceInfo,
    bleDevice = bleDevice
)
```

### 通过TCP连接使用

```kotlin
val bundle = CustomCommandLogPrintFragment.newBundleArguments(
    isIotCmd = true,
    type = ProductType.GT600,
    communicateWay = TcpConnect,
    deviceInfo = deviceInfo
)
```

## 测试建议

### 功能测试

#### 蓝牙模式
- [ ] 蓝牙连接状态显示正常
- [ ] 指令发送后能看到发送日志（蓝色）
- [ ] 设备响应后能看到接收日志（绿色）
- [ ] 调试模式切换功能正常
- [ ] 内置指令选择器可用
- [ ] 日志导出功能正常

#### TCP模式
- [ ] TCP连接成功显示"TCP 连接成功"（深绿色）
- [ ] 指令发送后能看到发送日志（蓝色）
- [ ] 设备响应后能看到接收日志（绿色）
- [ ] TCP断开显示"TCP 连接断开"（红色）
- [ ] TCP异常显示"TCP 连接异常"（红色）
- [ ] 原始字节数据能正确转换并显示
- [ ] 调试模式切换功能正常
- [ ] 内置指令选择器可用
- [ ] 日志导出功能正常

### 稳定性测试
- [ ] 快速切换发送指令不会崩溃
- [ ] 网络异常时不会崩溃
- [ ] 大量日志数据不会导致内存溢出
- [ ] 页面销毁时资源正确释放

### 边界测试
- [ ] 空数据响应处理正常
- [ ] 超长数据响应显示正常
- [ ] 特殊字符显示正常
- [ ] 快速重连场景处理正常

## 依赖关系

### 直接依赖
- `OptimizedBaseIOTDeviceFragment`: 提供基础通信能力
- `TcpViewModel`: TCP通信视图模型
- `BleViewModel`: 蓝牙通信视图模型
- `BleCustomCommandLogPrintViewModel`: 日志状态管理

### 间接依赖
- `DeviceCommunicationManager`: 统一通信管理器
- `TcpCommunicationStrategy`: TCP通信策略实现
- `BleCommunicationStrategy`: 蓝牙通信策略实现

## 注意事项

1. **指令序列发送**: 通过 `sendCommandSequence()` 方法发送，已经由 `DeviceCommunicationManager` 处理通信方式的切换
2. **响应数据监听**: 需要分别订阅不同通信方式的数据流
3. **生命周期管理**: 使用 `launchWithViewLifecycle` 确保观察者随页面生命周期正确管理
4. **错误处理**: 所有通信异常都会记录日志，不影响用户操作

## 参考代码

- `TcpDebugFragment.kt`: TCP调试页面的参考实现
- `OptimizedBaseIOTDeviceFragment.kt`: 基础设备Fragment，提供通信管理器
- `DeviceCommunicationManager.kt`: 通信管理器的策略选择实现

## 后续优化建议

1. **日志过滤**: 添加日志级别过滤功能（INFO/DEBUG/ERROR）
2. **自动重连**: TCP断开后可以考虑自动重连机制
3. **性能优化**: 大量日志时考虑分页或虚拟滚动
4. **数据格式化**: 对JSON或结构化数据进行格式化显示
5. **搜索功能**: 添加日志内容搜索功能

