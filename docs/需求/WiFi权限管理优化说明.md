# WiFi 权限管理优化说明

## 优化概述

将 `lib_wifi` 库的权限管理从**手动轮询模式**改造为**自动监听模式**，参考 `lib_ble` 库的架构实现，使用 `callbackFlow` 和 `BroadcastReceiver` 监听系统状态变化，实现自动通知 UI 层更新。

同时重构 WiFi 扫描仓库，使用 `MutableSharedFlow` 触发机制支持手动触发扫描，解决权限授予后无法自动扫描的问题。

**优化日期**: 2025/11/17  
**优化作者**: gonghe

---

## 优化前的问题

### 权限管理问题
1. `WifiPermissionManager` 使用简单的 `MutableStateFlow`，需要手动调用 `checkPermissions()` 更新状态
2. 无法自动响应系统状态变化（WiFi 开关、定位开关等）
3. UI 层需要手动轮询或在特定时机触发检查
4. 权限状态更新依赖于外部调用，不够智能

### WiFi 扫描问题
1. `WifiScannerRepository` 的 `callbackFlow` 只在订阅时执行一次扫描
2. `refresh()` 方法只清空数据，不触发新扫描
3. 权限授予后无法自动触发扫描
4. 页面在权限授予后停留在 Scanning 状态，无法显示结果

---

## 优化后的架构

### 核心组件

#### 1. WifiStateManager
**路径**: `lib_wifi/src/main/java/com/shmedo/lib/wifi/permission/wifi/WifiStateManager.kt`

**职责**:
- 使用 `callbackFlow` 创建 WiFi 状态流
- 监听 `WifiManager.WIFI_STATE_CHANGED_ACTION` 广播
- WiFi 开关状态变化时自动更新流
- 提供 `refreshPermission()` 手动刷新方法

**关键代码**:
```kotlin
fun wifiState() = callbackFlow {
    trySend(getWifiPermissionState())
    
    val wifiStateChangeHandler = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            trySend(getWifiPermissionState())
        }
    }
    val filter = IntentFilter().apply {
        addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        addAction(REFRESH_PERMISSIONS)
    }
    ContextCompat.registerReceiver(context, wifiStateChangeHandler, filter, ContextCompat.RECEIVER_EXPORTED)
    awaitClose { context.unregisterReceiver(wifiStateChangeHandler) }
}
```

#### 2. LocationStateManager
**路径**: `lib_wifi/src/main/java/com/shmedo/lib/wifi/permission/location/LocationStateManager.kt`

**职责**:
- 使用 `callbackFlow` 创建定位状态流
- 监听 `LocationManager.MODE_CHANGED_ACTION` 广播
- 检查定位权限和定位服务状态
- 提供权限永久拒绝检测方法

**关键功能**:
- 定位服务开关状态变化时自动更新流
- 标记定位权限已请求（用于判断是否被永久拒绝）
- 检测权限是否被用户选择"不再询问"

#### 3. WifiPermissionUtil
**路径**: `lib_wifi/src/main/java/com/shmedo/lib/wifi/permission/util/WifiPermissionUtil.kt`

**职责**:
- 提供静态工具方法检查 WiFi 和定位状态
- 检查 WiFi 是否开启
- 检查定位权限是否授予
- 检查定位服务是否开启

**关键属性**:
```kotlin
val isWifiAvailable: Boolean  // WiFi 是否开启
val isLocationEnabled: Boolean  // 定位服务是否开启
val isLocationPermissionGranted: Boolean  // 定位权限是否已授予
```

#### 4. WifiPermissionViewModel (重构)
**路径**: `lib_wifi/src/main/java/com/shmedo/lib/wifi/permission/viewmodel/WifiPermissionViewModel.kt`

**重大变更**:
- 注入 `WifiStateManager` 和 `LocationStateManager` 替代旧的 `WifiPermissionManager`
- 暴露两个独立的 StateFlow：`wifiState` 和 `locationState`
- 提供刷新方法

**新的 API**:
```kotlin
val wifiState: StateFlow<WifiPermissionState>  // WiFi 状态流
val locationState: StateFlow<WifiPermissionState>  // 定位状态流

fun refreshWifiPermission()  // 刷新 WiFi 状态
fun refreshLocationPermission()  // 刷新定位状态
```

#### 5. WifiScannerRepository (重构)
**路径**: `lib_wifi/src/main/java/com/shmedo/lib/wifi/scanner/repository/WifiScannerRepository.kt`

**重大变更**:
- 添加 `MutableSharedFlow<Unit>` 作为扫描触发器
- 使用 `flatMapLatest` 响应触发信号
- 每次触发都创建新的扫描流
- 支持手动触发扫描

**核心实现**:
```kotlin
private val scanTrigger = MutableSharedFlow<Unit>(replay = 1)

fun scanWifiNetworks(): Flow<WifiScanningState> = scanTrigger.flatMapLatest {
    callbackFlow {
        // 注册广播接收器
        // 启动 WiFi 扫描
        awaitClose { /* 取消注册 */ }
    }
}

fun startScan() {
    scanTrigger.tryEmit(Unit)
}
```

**关键特性**:
- `replay = 1`: 新订阅者立即收到触发事件，实现页面加载时自动扫描
- `flatMapLatest`: 每次触发取消之前的扫描，启动新扫描
- `tryEmit`: 非阻塞发送，可在非协程环境调用

#### 6. WifiScannerViewModel (重构)
**路径**: `lib_wifi/src/main/java/com/shmedo/lib/wifi/scanner/viewmodel/WifiScannerViewModel.kt`

**新增 API**:
```kotlin
fun startScan()  // 触发扫描，可在以下场景调用：
                 // - 用户下拉刷新
                 // - 权限授予后
                 // - 页面初次加载
```

---

## 架构优势

### 1. 自动响应式
- WiFi 开关变化时自动通知 UI
- 定位服务开关变化时自动通知 UI
- 权限就绪后自动触发扫描
- 无需手动轮询，降低耦合

### 2. 关注点分离
- `WifiStateManager` 专注 WiFi 状态
- `LocationStateManager` 专注定位状态
- `WifiScannerRepository` 专注扫描逻辑
- 各司其职，易于维护

### 3. 资源管理规范
- 使用 `callbackFlow` 的 `awaitClose` 确保 BroadcastReceiver 正确注销
- 使用 `WhileSubscribed(5000)` 策略自动管理扫描生命周期
- 避免内存泄漏

### 4. 线程安全
- 使用 `callbackFlow` 和 `trySend` 确保协程安全
- StateFlow 自动处理背压
- `MutableSharedFlow` 线程安全的触发机制

### 5. 易于测试
- 状态管理器可独立测试
- ViewModel 依赖注入，易于 Mock
- 扫描触发逻辑清晰可测

### 6. 可重复触发
- 支持手动触发 WiFi 扫描
- 权限授予后立即扫描
- 下拉刷新重新扫描
- 解决权限就绪后无法扫描的问题

---

## Koin 配置更新

**文件**: `lib_wifi/src/main/java/com/shmedo/lib/wifi/koin/LibWifiKoinModule.kt`

**变更**:
```kotlin
// 移除旧的
single { WifiPermissionManager(androidContext()) }

// 添加新的
single { WifiStateManager(androidContext()) }
single { LocationStateManager(androidContext()) }
viewModel { WifiPermissionViewModel(get(), get()) }
```

---

## 迁移指南

### UI 层使用变更

#### 旧的用法（已废弃）
```kotlin
// 需要手动调用检查
viewModel.checkPermissions()

// 只有一个状态流
viewModel.permissionState.collect { state ->
    // 处理状态
}
```

#### 新的用法（推荐）
```kotlin
// 无需手动调用，自动监听系统变化

// WiFi 状态
viewModel.wifiState.collect { state ->
    when (state) {
        is WifiPermissionState.Available -> {
            // WiFi 可用
        }
        is WifiPermissionState.NotAvailable -> {
            when (state.reason) {
                WifiPermissionNotAvailableReason.WifiDisabled -> {
                    // WiFi 未开启
                }
            }
        }
    }
}

// 定位状态（关键：自动触发扫描）
viewModel.locationState.collect { state ->
    when (state) {
        is WifiPermissionState.Available -> {
            // 定位权限和服务可用
            // 【重要】自动触发扫描
            scannerViewModel.startScan()
        }
        is WifiPermissionState.NotAvailable -> {
            when (state.reason) {
                WifiPermissionNotAvailableReason.PermissionRequired -> {
                    // 需要定位权限
                }
                WifiPermissionNotAvailableReason.LocationServiceDisabled -> {
                    // 定位服务未开启
                }
            }
        }
    }
}

// 权限授予后手动刷新（会触发 locationState 变化，进而自动扫描）
viewModel.refreshLocationPermission()
```

### 权限请求流程

```kotlin
// 1. 请求权限
XXPermissions.with(this)
    .permission(Manifest.permission.ACCESS_FINE_LOCATION)
    .interceptor(PermissionInterceptor())
    .description(PermissionDescription())
    .request(object : OnPermissionCallback {
        override fun onResult(
            grantedList: List<IPermission>, 
            deniedList: List<IPermission>
        ) {
            if (deniedList.isEmpty()) {
                // 2. 权限授予后刷新状态
                permissionViewModel.refreshLocationPermission()
                // locationState 会变为 Available
                // 在 locationState.collect 中会自动触发扫描
            } else {
                // 权限被拒绝
            }
        }
    })
```

### WiFi 扫描触发流程

```kotlin
// 方式 1: 用户下拉刷新
private fun refreshScan() = launchWithViewLifecycle {
    scannerViewModel.startScan()
    delay(3000)
    binding.refreshLayout.finish()
}

// 方式 2: 权限就绪时自动触发（推荐）
private fun handleLocationState(state: WifiPermissionState) {
    when (state) {
        is WifiPermissionState.Available -> {
            // 【自动触发】权限和服务都就绪时
            scannerViewModel.startScan()
        }
        // ...
    }
}

// 方式 3: 页面初次加载时（自动，无需手动调用）
// scanTrigger 的 replay = 1 确保新订阅者立即收到触发事件
```

---

## 技术细节

### 广播接收器注册
使用 `ContextCompat.RECEIVER_EXPORTED` 确保 Android 13+ 兼容性：
```kotlin
ContextCompat.registerReceiver(
    context,
    receiver,
    filter,
    ContextCompat.RECEIVER_EXPORTED
)
```

### 手动刷新机制
通过自定义广播实现手动刷新：
```kotlin
private const val REFRESH_PERMISSIONS = "com.shmedo.lib.wifi.permission.REFRESH_*_PERMISSIONS"

fun refreshPermission() {
    val intent = Intent(REFRESH_PERMISSIONS)
    context.sendBroadcast(intent)
}
```

### 扫描触发机制
使用 `MutableSharedFlow` + `flatMapLatest` 实现可重复触发：
```kotlin
// 触发器配置
private val scanTrigger = MutableSharedFlow<Unit>(
    replay = 1,           // 保留最后一个值
    extraBufferCapacity = 0,  // 不需要额外缓冲
    onBufferOverflow = BufferOverflow.DROP_OLDEST  // 丢弃最旧的值
)

// 扫描流配置
fun scanWifiNetworks(): Flow<WifiScanningState> = 
    scanTrigger.flatMapLatest {  // 每次触发都取消之前的扫描
        callbackFlow {
            // 扫描逻辑
        }
    }
```

### StateFlow 配置
```kotlin
// 权限状态流：懒加载
stateIn(
    viewModelScope,
    SharingStarted.Lazily,  // 懒加载，有订阅者时才开始
    initialValue  // 初始值
)

// 扫描状态流：自动管理生命周期
stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),  // 无订阅者 5 秒后停止
    WifiScanningState.Idle  // 初始状态
)
```

---

## 文件变更清单

### 新增文件
- `lib_wifi/src/main/java/com/shmedo/lib/wifi/permission/wifi/WifiStateManager.kt`
- `lib_wifi/src/main/java/com/shmedo/lib/wifi/permission/location/LocationStateManager.kt`
- `lib_wifi/src/main/java/com/shmedo/lib/wifi/permission/util/WifiPermissionUtil.kt`
- `lib_wifi/src/main/java/com/shmedo/lib/wifi/permission/util/LocalDataProvider.kt`

### 重构文件
- `lib_wifi/src/main/java/com/shmedo/lib/wifi/permission/viewmodel/WifiPermissionViewModel.kt` (权限管理重构)
- `lib_wifi/src/main/java/com/shmedo/lib/wifi/scanner/repository/WifiScannerRepository.kt` (扫描触发重构)
- `lib_wifi/src/main/java/com/shmedo/lib/wifi/scanner/viewmodel/WifiScannerViewModel.kt` (添加 startScan 方法)
- `app/src/main/java/com/shmedo/mcloudapp/ui/page/device/WiFiScannerListFragment.kt` (使用新 API)

### 优化文件
- `lib_wifi/src/main/java/com/shmedo/lib/wifi/permission/WifiPermissionState.kt`
- `lib_wifi/src/main/java/com/shmedo/lib/wifi/koin/LibWifiKoinModule.kt`

### 删除文件
- `lib_wifi/src/main/java/com/shmedo/lib/wifi/permission/WifiPermissionManager.kt` (已废弃)

---

## 测试建议

### 单元测试
1. 测试 `WifiPermissionUtil` 的权限检查逻辑
2. 测试 `LocalDataProvider` 的数据持久化
3. 测试 `WifiPermissionViewModel` 的状态转换
4. 测试 `WifiScannerRepository.startScan()` 触发机制
5. 测试 `scanTrigger` 的 replay 行为

### 集成测试
1. 测试 WiFi 开关切换时状态更新
2. 测试定位服务开关切换时状态更新
3. 测试权限授予/拒绝流程
4. 测试权限授予后自动触发扫描

### 手动测试场景（关键）

#### 权限管理测试
1. 在 WiFi 扫描页面，关闭 WiFi，观察 UI 提示
2. 在 WiFi 扫描页面，关闭定位服务，观察 UI 提示
3. 在 WiFi 扫描页面，拒绝定位权限，观察 UI 提示
4. 从设置页面返回后，自动刷新状态

#### 扫描触发测试（重点）
1. **权限未授予 → 授予权限**
   - 进入页面（无定位权限）
   - 点击授予权限按钮
   - 授予定位权限
   - **预期**：立即开始扫描并显示结果（不再停留在刷新状态）

2. **定位服务未开启 → 开启服务**
   - 进入页面（定位服务关闭）
   - 打开定位服务
   - **预期**：返回后自动扫描并显示结果

3. **下拉刷新**
   - 在有权限的情况下
   - 下拉刷新列表
   - **预期**：重新扫描并更新列表

4. **页面初次加载**
   - 在权限已就绪的情况下
   - 进入 WiFi 扫描页面
   - **预期**：自动开始扫描

5. **搜索过滤**
   - 扫描完成后
   - 输入搜索关键词
   - **预期**：过滤结果，不触发新扫描

6. **频率限制测试**
   - 快速多次下拉刷新（短时间内 > 4 次）
   - **预期**：Android 10+ 可能出现频率限制提示

---

## 向后兼容性

- 保持 `WifiPermissionState` 的密封类结构不变
- UI 层只需更新 ViewModel 调用方式
- 旧的 `WifiPermissionManager` 已删除，升级后需要更新代码
- 扫描触发机制向后兼容，不影响现有订阅者

---

## 注意事项

1. **Android 版本要求**: 最低支持 Android 10 (API 29)
2. **权限要求**: WiFi 扫描需要 `ACCESS_FINE_LOCATION` 权限
3. **MMKV 初始化**: 确保应用启动时初始化 MMKV
4. **广播权限**: Android 13+ 需要 `RECEIVER_EXPORTED` 标志
5. **扫描频率限制**: Android 10+ 限制为 120 秒内最多 4 次扫描
6. **自动扫描时机**: 权限就绪时会自动触发扫描，避免在不合适的时机手动调用
7. **刷新延迟**: 建议使用 3 秒延迟，给扫描足够的时间完成

---

## 参考资料

- [Nordic BLE Library Permission](https://github.com/NordicSemiconductor/Android-BLE-Library)
- [Kotlin Coroutines Flow](https://kotlinlang.org/docs/flow.html)
- [Android Broadcast Receiver](https://developer.android.com/guide/components/broadcasts)
- [SharedFlow vs StateFlow](https://kotlinlang.org/docs/shared-flow.html)
- [flatMapLatest 操作符](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/flat-map-latest.html)
- [lib_ble 权限管理实现](../lib_ble/src/main/java/com/shmedo/lib/ble/permission/)
- [WifiScannerRepository 重构说明](./WifiScannerRepository重构说明.md)
- [WifiScannerRepository 重构总结](./WifiScannerRepository重构总结.md)

---

## 总结

本次优化显著提升了 WiFi 权限管理和扫描触发的用户体验和代码质量：

### 权限管理优化
- ✅ 实现自动监听，无需手动轮询
- ✅ 分离关注点，代码更清晰
- ✅ 资源管理规范，避免泄漏
- ✅ 线程安全，协程友好
- ✅ 易于测试和维护

### 扫描触发优化
- ✅ 支持手动触发扫描，解决权限授予后无法扫描的问题
- ✅ 使用响应式架构，保持 Flow 特性
- ✅ 多场景自动扫描（权限就绪、下拉刷新、页面加载）
- ✅ 生命周期安全，自动管理订阅
- ✅ 易于扩展，可支持定期扫描

优化后的架构与 `lib_ble` 保持一致，便于团队理解和维护。权限管理和扫描触发完美配合，实现了从权限授予到扫描执行的全自动流程。

