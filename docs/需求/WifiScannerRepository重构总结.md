# WiFi 扫描仓库重构总结

## 问题
权限授予后，WiFi 扫描页面一直处于刷新状态，无法显示扫描结果。

## 原因
- WiFi 扫描是一次性的，原架构的 `callbackFlow` 只在订阅时扫描一次
- `refresh()` 方法只清空数据，不触发新扫描
- 权限授予后没有重新触发扫描

## 解决方案
使用 `MutableSharedFlow` + `flatMapLatest` 实现可重复触发的扫描机制

## 改动文件

### 1. WifiScannerRepository.kt
```kotlin
// 添加触发器
private val scanTrigger = MutableSharedFlow<Unit>(replay = 1)

// 改造扫描方法
fun scanWifiNetworks(): Flow<WifiScanningState> = scanTrigger.flatMapLatest {
    callbackFlow { /* 原有逻辑 */ }
}

// 添加触发方法
fun startScan() {
    scanTrigger.tryEmit(Unit)
}
```

### 2. WifiScannerViewModel.kt
```kotlin
// 添加扫描方法
fun startScan() {
    scannerRepository.startScan()
}

// 废弃旧方法
@Deprecated("使用 startScan() 替代")
fun refresh() {
    scannerRepository.clear()
    startScan()
}
```

### 3. WiFiScannerListFragment.kt
```kotlin
// 刷新时触发扫描
private fun refreshScan() {
    scannerViewModel.startScan()
    // ...
}

// 权限授予后触发扫描
private fun requestLocationPermission() {
    // ...
    if (allGranted) {
        permissionViewModel.refreshLocationPermission()
        scannerViewModel.startScan()  // 新增
    }
}

// 权限就绪时自动扫描
private fun handleLocationState(state: WifiPermissionState) {
    when (state) {
        is WifiPermissionState.Available -> {
            scannerViewModel.startScan()  // 新增
        }
    }
}
```

## 扫描触发时机

✅ 页面初次加载（权限已就绪）  
✅ 用户下拉刷新  
✅ 用户授予权限后  
✅ 从设置返回（定位服务开启后）

## 技术亮点

- **响应式架构**：保持 Flow 的响应式特性
- **生命周期安全**：使用 `WhileSubscribed(5000)` 自动管理
- **向后兼容**：保留废弃方法，使用 `@Deprecated` 引导
- **易于扩展**：可轻松支持定期扫描

## 测试要点

1. ✅ 权限未授予 → 授予权限 → 应立即扫描
2. ✅ 定位服务关闭 → 开启服务 → 返回后自动扫描
3. ✅ 下拉刷新 → 重新扫描
4. ✅ 搜索过滤 → 不触发新扫描
5. ⚠️ 注意 Android 10+ 扫描频率限制（120秒内最多4次）

## 相关文档
- [详细重构说明](./WifiScannerRepository重构说明.md)
- [WiFi权限管理优化说明](./WiFi权限管理优化说明.md)

