---
trigger: always_on
---

# Android 项目架构模式和技术栈指南

## 架构模式
- **MVVM**: Model-View-ViewModel 架构模式
- **Repository Pattern**: 数据访问层抽象
- **依赖注入**: 使用 Koin 进行依赖管理

## 技术栈

### 核心框架
- **Kotlin**: 主要开发语言，遵循官方编码约定
- **Android Jetpack**: 
  - ViewModel + LiveData/StateFlow
  - Navigation Component
  - Room 数据库
  - DataBinding/ViewBinding

### 网络通信
- **HTTP**: RxHttp + OkHttp + Moshi
- **BLE**: Nordic BLE Library (ble-ktx)
- **TCP**: Netty 框架

### UI 组件
- **Material Design**: 遵循 Material Design 3 规范
- **RecyclerView**: 使用 BRV 库简化列表开发
- **图片加载**: Glide
- **弹窗组件**: XPopup, DialogX, Material Dialogs

### 数据存储
- **关系型数据**: Room 数据库
- **键值存储**: MMKV
- **配置管理**: SharedPreferences

### 工具库
- **日志**: Timber
- **工具类**: UtilCodeX
- **权限**: XXPermissions
- **崩溃收集**: Bugly
- **地图服务**: 百度地图 SDK

## 模块依赖关系

```
app (主应用)
├── core_data (数据层)
│   ├── core_model (数据模型)
│   └── core_commonlib (通用工具)
├── lib_ble (蓝牙通信)
├── lib_tcp (TCP通信)
├── lib_network (网络请求)
└── lib_cmd (设备指令)
```

## 包结构规范

```kotlin
com.shmedo.mcloudapp/
├── ui/page/           // 页面层
│   ├── base/          // 基础 UI 组件
│   ├── device/        // 设备相关页面
│   └── mine/          // 个人中心
├── ui/viewmodel/      // ViewModel 层
├── ui/widget/         // 自定义控件
├── model/             // 数据模型
├── utils/             // 工具类
└── koin/              // 依赖注入配置
```

## 异步编程
- 优先使用 Kotlin Coroutines
- ViewModel 中使用 `viewModelScope`
- Repository 中使用 `suspend` 函数
- UI 更新使用 LiveData/StateFlow

## 错误处理
- 统一异常处理机制
- 网络错误重试策略
- 用户友好的错误提示
- 崩溃信息收集和上报