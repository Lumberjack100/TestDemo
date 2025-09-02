# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 通用原则
- **代码整洁**: 遵循 Clean Code 原则，编写可读、可维护的代码。
- **错误处理**: 实施完善的错误处理机制，确保应用的健壮性。
- **异步编程**: 在 Kotlin 中，优先使用协程 (Coroutines) 和 `async/await` 模式处理异步操作，而不是传统的回调方法。
- **注释**: 添加必要的注释，解释其意图和复杂逻辑。
- **性能**: 添加必要的注释，解释其意图和复杂逻辑。

## Kotlin 特定
- 遵循官方的 Kotlin 编码约定。
- 合理使用 Kotlin 的特性，如扩展函数、数据类、密封类等，以提高代码的简洁性和表达力。

## 项目架构

### 模块化结构
```
mCloudapp_V5/
├── app/                    # 主应用模块 - UI层和业务逻辑
├── core_commonlib/         # 核心通用库 - 工具类和扩展函数
├── core_data/              # 数据处理层 - Repository和数据源
├── core_model/             # 数据模型层 - 实体类和数据结构
├── lib_ble/                # 蓝牙通信库 - BLE设备通信
├── lib_cmd/                # 设备指令库 - 设备命令封装
├── lib_network/            # 网络请求库 - HTTP通信
└── lib_tcp/                # TCP通信库 - TCP长连接
```

### 架构模式
- **MVVM**: ViewModel + LiveData/StateFlow + DataBinding
- **Repository Pattern**: 统一数据访问入口
- **依赖注入**: 使用 Koin 框架管理依赖

### 主要技术栈
- **Language**: Kotlin (主要) + Java (少量)
- **UI**: Material Design Components, ViewBinding, DataBinding
- **Network**: RxHttp + OkHttp + Moshi
- **BLE**: Nordic BLE Library (ble-ktx)
- **Database**: Room + MMKV
- **DI**: Koin
- **Maps**: 百度地图SDK
- **Image**: Glide
- **Async**: Kotlin Coroutines

## 开发命令

### 构建和运行
```bash
# 构建项目
./gradlew build

# 构建Debug版本
./gradlew assembleDebug

# 构建Release版本
./gradlew assembleRelease

# 清理项目
./gradlew clean

# 运行测试
./gradlew test

# 运行Android测试
./gradlew connectedAndroidTest
```

### 产品变体
项目有两个产品变体：
- **production**: 正式发布版本 (包名: com.shmedo.mcloudapp.iot)
- **demo**: 测试版本 (包名: com.shmedo.mcloudapp.iot.v5)

构建特定变体：
```bash
# 构建生产环境Debug版本
./gradlew assembleProductionDebug

# 构建Demo环境Debug版本  
./gradlew assembleDemoDebug
```

## 关键架构组件

### 依赖注入 (Koin)
- 主模块: `app/src/main/java/com/shmedo/mcloudapp/koin/AppKoinModule.kt`
- ViewModel模块: `app/src/main/java/com/shmedo/mcloudapp/koin/ViewModelModule.kt`
- 各功能模块都有对应的Koin模块

### 通信架构
- **BLE通信**: `lib_ble/` - 蓝牙低功耗设备通信
- **TCP通信**: `lib_tcp/` - TCP长连接通信
- **HTTP通信**: `lib_network/` - RESTful API通信
- **命令系统**: `lib_cmd/` - 设备指令封装和解析

### 数据层
- **Repository**: `core_data/src/main/java/com/shmedo/core/data/repository/`
- **Local DB**: Room数据库，MMKV键值存储
- **Remote API**: RxHttp网络请求

### UI基类体系
- **BaseActivity**: `app/src/main/java/com/shmedo/mcloudapp/ui/page/base/activity/BaseActivity.kt`
- **BaseFragment**: 位于 `app/src/main/java/com/shmedo/mcloudapp/ui/page/base/fragment/`
- **BaseViewModel**: `app/src/main/java/com/shmedo/mcloudapp/ui/page/base/viewmodel/BaseStateViewModel.kt`

### 设备支持
项目支持多种物联网设备：
- ADME系列 (自动深度测量设备)
- DAS系列 (数据采集站)
- GNSS设备 (M20, M50等)
- UD系列 (超声波设备)
- MR系列 (多路记录仪)

## 开发注意事项

### 文件命名
- Activity: `*Activity.kt`
- Fragment: `*Fragment.kt`
- ViewModel: `*ViewModel.kt`
- Repository: `*Repository.kt`

### 包结构
```
com.shmedo.mcloudapp/
├── ui/page/            # UI页面
│   ├── base/          # 基础UI组件
│   ├── device/        # 设备相关页面
│   └── mine/          # 个人中心页面
├── ui/viewmodel/      # ViewModel层
├── ui/widget/         # 自定义控件
├── model/             # 数据模型
├── utils/             # 工具类
└── koin/              # 依赖注入配置
```

### 版本控制
项目使用Git版本控制，版本号基于Git提交数量自动生成：
- versionCode: Git提交总数
- versionName: `majorVersion.minorVersion.patchVersion`

### 调试和日志
- 使用 Timber 进行日志记录
- 集成 Bugly 进行崩溃收集