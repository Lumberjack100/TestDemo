# WiFi 扫描仓库重构说明

**重构日期**: 2025/11/17
**重构人员**: gonghe  

## 问题描述

### 原始问题
在测试 `WiFiScannerListFragment` 时，发现当定位权限未授予时，手动授予权限后，页面状态一直处于刷新中（Scanning 状态），无法显示扫描结果。

### 问题场景
1. 用户打开 WiFi 扫描页面
2. 定位权限未授予
3. 页面可能已尝试扫描或用户手动下拉刷新
4. 扫描因权限不足无法获取结果
5. 用户授予定位权限
6. **问题**：权限状态更新了，但页面仍处于刷新状态，没有扫描结果

### 根本原因分析

#### 1. WiFi 扫描的特性
- **BLE 扫描**：是持续的，设备会不断广播信号，扫描回调会持续返回结果
- **WiFi 扫描**：是一次性的，调用 `startScan()` 后等待一个扫描结果广播就结束了

#### 2. 原有架构的问题

**WifiScannerRepository (原实现)**:
```kotlin
fun scanWifiNetworks(): Flow<WifiScanningState> = callbackFlow {
    // 注册广播接收器
    // 启动一次扫描
    wifiManager.startScan()
    
    awaitClose { 
        // 取消注册
    }
}
```

问题：
- `callbackFlow` 在被订阅时**只执行一次扫描**
- 之后只是被动监听扫描结果广播
- 无法主动触发新的扫描

**WifiScannerViewModel (原实现)**:
```kotlin
fun refresh() {
    scannerRepository.clear()  // 只清空数据存储
}
```

问题：
- `refresh()` 方法只清空数据，**没有重新触发扫描**
- 无法响应权限授予后的场景

**WiFiScannerListFragment (原实现)**:
```kotlin
private fun requestLocationPermission() {
    // 权限授予回调
    if (allGranted) {
        permissionViewModel.refreshLocationPermission()
        // 没有触发新扫描
    }
}
```

问题：
- 权限授予后只刷新了权限状态
- 没有触发新的扫描
- 导致页面停留在 Scanning 状态

## 重构方案

### 设计思路

采用 **响应式触发机制**：
- 使用 `MutableSharedFlow` 作为扫描触发器
- 使用 `flatMapLatest` 响应触发信号
- 每次触发都创建新的扫描流
- 保持原有的响应式架构风格

### 架构对比

#### 重构前
```
Fragment -> ViewModel.refresh() -> Repository.clear()
                                   (只清空数据，不扫描)
```

#### 重构后
```
Fragment -> ViewModel.startScan() -> Repository.startScan()
                                   -> scanTrigger.emit()
                                   -> flatMapLatest { callbackFlow }
                                   -> 执行新的扫描
```

## 实现细节

### 1. WifiScannerRepository 重构

**添加扫描触发器**:
```kotlin
private val scanTrigger = MutableSharedFlow<Unit>(replay = 1)
```

**改造扫描方法**:
```kotlin
fun scanWifiNetworks(): Flow<WifiScanningState> = scanTrigger.flatMapLatest {
    callbackFlow {
        // 原有的扫描逻辑
    }
}
```

**添加触发方法**:
```kotlin
fun startScan() {
    Timber.d("触发 WiFi 扫描")
    scanTrigger.tryEmit(Unit)
}
```

**关键点**:
- `replay = 1`: 确保新订阅者也能立即收到最近的触发事件，页面初次加载时自动扫描
- `flatMapLatest`: 每次触发都取消之前的扫描，创建新的扫描流
- `tryEmit`: 非阻塞发送，适合在非协程环境调用

### 2. WifiScannerViewModel 重构

**添加扫描方法**:
```kotlin
fun startScan() {
    Timber.i("ViewModel 触发扫描")
    scannerRepository.startScan()
}
```

**废弃旧方法**:
```kotlin
@Deprecated(
    message = "使用 startScan() 替代",
    replaceWith = ReplaceWith("startScan()")
)
fun refresh() {
    scannerRepository.clear()
    startScan()  // 清空后触发扫描
}
```

### 3. WiFiScannerListFragment 调整

**刷新扫描**:
```kotlin
private fun refreshScan() = launchWithViewLifecycle {
    Timber.i("刷新扫描")
    scannerViewModel.startScan()  // 使用新 API
    delay(1000)
    binding.refreshLayout.finish()
}
```

**权限授予后触发扫描**:
```kotlin
private fun requestLocationPermission() {
    XXPermissions.with(this)
        .request(object : OnPermissionCallback {
            override fun onResult(grantedList: List<IPermission>, deniedList: List<IPermission>) {
                if (deniedList.isEmpty()) {
                    permissionViewModel.refreshLocationPermission()
                    
                    // 【关键修复】权限授予后立即触发扫描
                    scannerViewModel.startScan()
                }
            }
        })
}
```

**权限状态变化时自动扫描**:
```kotlin
private fun handleLocationState(state: WifiPermissionState) {
    when (state) {
        is WifiPermissionState.Available -> {
            // 【关键优化】当定位权限和服务都就绪时，自动触发扫描
            scannerViewModel.startScan()
        }
        // ...
    }
}
```

## 触发扫描的时机

经过重构，扫描会在以下场景自动触发：

1. **页面初次加载**
   - 如果权限已就绪，`handleLocationState` 会自动触发扫描
   - `scanTrigger` 的 `replay = 1` 确保订阅时立即触发

2. **用户下拉刷新**
   - 调用 `refreshScan()` -> `startScan()`

3. **用户授予权限**
   - 权限请求回调中调用 `startScan()`
   - 权限状态变为 Available 时也会触发

4. **从设置返回**
   - 如果定位服务被开启，状态变为 Available 时触发

## 技术亮点

### 1. 响应式架构
- 保持 Flow 的响应式特性
- 符合 Kotlin Coroutines 的设计理念
- 与 BLE 模块架构一致，便于维护

### 2. 生命周期管理
- 使用 `WhileSubscribed(5000)` 策略
- Fragment 销毁时自动停止扫描
- 避免内存泄漏

### 3. 可扩展性
- 如果将来需要定期扫描，可以轻松扩展：
  ```kotlin
  fun startPeriodicScan(interval: Long) {
      viewModelScope.launch {
          while (isActive) {
              startScan()
              delay(interval)
          }
      }
  }
  ```

## 测试建议

### 测试场景

1. **权限未授予时授予权限**
   - 进入页面（无权限）
   - 授予定位权限
   - **预期**：立即开始扫描并显示结果

2. **定位服务未开启时开启服务**
   - 进入页面（定位服务关闭）
   - 打开定位服务
   - **预期**：返回后自动扫描

3. **下拉刷新**
   - 在有权限的情况下
   - 下拉刷新列表
   - **预期**：重新扫描并更新列表

4. **搜索过滤**
   - 扫描完成后
   - 输入搜索关键词
   - **预期**：过滤结果，不触发新扫描

5. **频率限制测试**
   - 快速多次下拉刷新
   - **预期**：Android 10+ 可能出现频率限制提示

### 性能考量

- **扫描频率限制**：Android 10+ 限制为 120 秒内最多 4 次
- **电量消耗**：只在需要时扫描，不会持续扫描
- **内存占用**：使用 `WhileSubscribed` 策略，无订阅者时自动释放


## 总结

这次重构通过引入 `MutableSharedFlow` 触发机制，解决了 WiFi 扫描无法重复触发的根本问题。重构后的架构：

✅ **解决了原始问题**：权限授予后能立即扫描  
✅ **保持响应式架构**：符合 Kotlin Flow 的设计理念  
✅ **易于维护**：与 BLE 模块架构一致  
✅ **自动化程度高**：多个场景自动触发扫描  
✅ **生命周期安全**：自动管理订阅和取消  
✅ **向后兼容**：不影响现有代码  

**建议**：
- 在其他类似场景中可以参考这个模式
- 注意 Android 10+ 的扫描频率限制
- 考虑添加扫描冷却时间，避免触发系统限制

