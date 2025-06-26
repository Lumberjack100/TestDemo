# 米易通物联网应用 (mCloudapp V5)

## 项目概述
米易通物联网应用是一款面向IoT设备的管理Android移动应用，主要用于连接、管理各类物联网监测设备。该应用支持通过4G网络和蓝牙两种方式连接设备，实现对设备的参数配置、数据查询和远程控制等功能。

### 目标用户

- 工程技术人员
- 企业运维人员

### 应用价值

- 提供统一的设备管理平台，简化设备连接和配置流程
- 实时监控设备状态和数据，及时发现异常
- 支持远程配置和控制，减少现场操作需求
- 数据查询和分析功能，辅助决策和问题诊断
- 提升设备运维效率，降低人力成本

## 安装与部署

### 系统要求
- Android 6.0 (API 23) 或更高版本
- 支持蓝牙4.0及以上
- 至少1GB可用存储空间
- 建议2GB或更多RAM

### 安装方式
1. **直接安装APK**：从发布页面下载最新版本APK文件，在Android设备上安装
2. **应用商店**：从Google Play或其他应用商店搜索"米易通"下载安装
3. **开发环境构建**：克隆代码库，使用Android Studio构建并部署

### 权限说明
应用需要以下关键权限：
- 蓝牙权限（扫描、连接设备）
- 位置权限（BLE扫描需要）
- 存储权限（数据存储和日志）
- 网络权限（与远程设备通信）
- 相机权限（扫描二维码）

## 技术架构

### 开发环境
- **编程语言**: Kotlin 
- **构建工具**: Gradle 8.x
- **最低支持Android版本**: 见 `libs.versions.minSdk`
- **目标Android版本**: 见 `libs.versions.targetSdk`
- **开发IDE**: Android Studio Iguana | 2023.2.1 或更高版本

### 主要架构与模式
- **MVVM 架构**: 使用 ViewModel 和 LiveData/StateFlow 实现数据绑定和UI更新
- **模块化结构**: 将功能按照职责划分为不同模块，降低耦合度
- **Repository 模式**: 统一数据访问接口，抽象数据源
- **依赖注入**: 使用 Koin 实现依赖注入
- **协程(Coroutines)**: 优先使用协程和 async/await 模式处理异步操作
- **单向数据流**: UI状态采用不可变状态模式

### 关键技术框架与库
- **网络通信**: RxHttp, OkHttp
- **数据存储**: Room 数据库, MMKV
- **UI组件**: Material Design, FlexBox, BRV (RecyclerView框架)
- **导航组件**：Navigation Component
- **图片加载**: Glide
- **蓝牙通信**: 自研 BLE 库
- **位置服务**: 百度地图 SDK
- **事件监听**: Lifecycle 组件
- **工具类**: AndroidUtilCode
- **日志记录**: Timber
- **JSON解析**: Moshi
- **权限管理**：XXPermissions
- **日志系统**：Timber
- **异常上报**：Bugly
- **TCP通信**: Netty

## 功能特性

### 核心功能
- **设备连接与管理**: 支持BLE、TCP和云端API方式连接各类设备
- **设备配置**: 提供友好的界面进行设备参数配置
- **实时数据监控**: 可视化展示设备状态和传感器数据
- **数据分析**: 历史数据查询与趋势分析
- **固件升级**: OTA方式更新设备固件
- **告警管理**: 设备异常告警与通知
- **远程控制**: 对支持的设备进行远程操作
- **离线工作**: 支持部分功能在无网络环境下工作

### 支持的设备类型
- MR702 遥测终端机
- M50一体式 GNSS 监测站
- 一体式多参数裂缝计(LR200)
- 一体式雷达水位计
- 自组网报警网关
- ADME 自动化测斜机器人
- DAS设备
- 遥测终端倾斜仪(MR501)
- BHY-3S设备
- U系列通用设备

## 项目结构

### 模块说明
- **app**: 应用主模块，包含UI界面和应用逻辑
  - 提供用户界面和交互
  - 实现设备管理和数据展示
  - 包含设备特定的UI组件

- **core_data**: 数据层，负责本地存储和远程数据获取
  - 实现Repository模式
  - 使用Room处理本地数据持久化
  - 提供统一的数据访问接口

- **core_model**: 定义实体模型类
  - 包含所有数据模型和实体定义
  - 定义枚举类型和常量
  - 提供IoT设备相关的数据结构

- **core_commonlib**: 通用工具类和基础组件
  - 提供工具方法和扩展函数
  - 实现通用UI组件
  - 包含日志、文件处理等基础功能

- **lib_ble**: 蓝牙通信模块，处理蓝牙设备连接和通信
  - 实现BLE设备扫描
  - 处理设备连接与断开
  - 管理特征值读写操作
  - 处理BLE权限

- **lib_cmd**: 设备指令处理库
  - 定义设备通信协议
  - 实现指令的编码与解码
  - 处理不同设备类型的指令差异

- **lib_tcp**: TCP通信模块，处理网络设备连接和通信
  - 基于Netty实现TCP客户端
  - 处理连接状态管理
  - 实现数据包的发送与接收

- **lib_network**: 网络请求库
  - 封装HTTP请求
  - 处理API响应与错误
  - 实现拦截器和缓存策略

### 主要目录说明
```
mCloudapp_V5/
├── app/                       # 应用主模块
│   └── src/main/
│       ├── assets/           # 静态资源文件
│       ├── java/com/shmedo/mcloudapp/
│       │   ├── ui/           # 界面相关类
│       │   │   ├── adapter/  # 适配器
│       │   │   ├── dialog/   # 对话框
│       │   │   ├── page/     # 页面
│       │   │   │   ├── device/ # 设备相关页面
│       │   │   │   ├── login/  # 登录相关
│       │   │   │   └── main/   # 主页面
│       │   │   ├── viewmodel/ # 视图模型
│       │   │   └── widget/    # 自定义控件
│       │   ├── model/        # 应用级数据模型
│       │   ├── utils/        # 工具类
│       │   ├── extensions/   # Kotlin扩展函数
│       │   ├── koin/         # 依赖注入配置
│       │   └── MCloudApplication.kt # 应用入口类
│       └── res/              # 资源文件
├── core_data/                 # 数据处理模块
│   └── src/main/java/com/shmedo/core/data/
│       ├── repository/       # 数据仓库实现
│       ├── source/           # 数据源
│       │   ├── local/        # 本地数据源
│       │   │   ├── dao/      # 数据访问对象
│       │   │   └── entity/   # 数据库实体
│       │   └── remote/       # 远程数据源
│       └── koin/             # 数据层依赖注入
├── core_model/                # 数据模型定义
│   └── src/main/java/com/shmedo/core/model/
│       ├── iotmodel/         # IoT设备模型
│       └── enums/            # 枚举类型
├── core_commonlib/            # 通用库
│   └── src/main/java/com/shmedo/core/commonlib/
│       ├── extensions/       # 扩展函数
│       ├── jsonhelper/       # JSON处理
│       ├── mmkv/             # MMKV封装
│       └── utils/            # 工具类
├── lib_ble/                   # 蓝牙通信
│   └── src/main/java/com/shmedo/lib/ble/
│       ├── communicate/      # 通信实现
│       ├── scanner/          # 扫描功能
│       └── permission/       # 权限处理
├── lib_cmd/                   # 命令处理
│   └── src/main/java/com/shmedo/lib/cmd/
│       ├── base/             # 基础命令
│       │   ├── iot_cmd/      # IoT设备命令
│       │   └── md_cmd/       # 其他设备命令
├── lib_tcp/                   # TCP通信
│   └── src/main/java/com/shmedo/lib/tcp/
│       └── netty/            # Netty实现
└── lib_network/               # 网络请求
    └── src/main/java/com/shmedo/lib/network/
        ├── interceptor/      # 网络拦截器
        ├── parser/           # 响应解析
        └── response/         # 响应模型
```

## 主要流程与模式

### 设备连接流程
1. 通过蓝牙扫描/TCP连接/云端API获取设备信息
2. 建立通信连接
3. 发送认证信息
4. 建立会话并维持心跳
5. 断开连接时进行资源释放

### 数据处理流程
1. 从设备获取原始数据
2. 根据设备类型和传感器模型解析数据
3. 存储到本地数据库
4. UI层通过观察LiveData/StateFlow获取并显示数据
5. 必要时将数据上传至云端

### 错误处理策略
1. 通信层错误：重试机制，指数退避策略
2. 业务层错误：统一错误码处理，友好提示
3. UI层错误：优雅降级，保持基本功能可用
4. 崩溃处理：Bugly收集异常，便于分析和修复

## 性能优化

### 内存管理
- 使用弱引用避免内存泄漏
- 大型数据集采用分页加载
- 图片压缩和缓存策略
- 内存敏感操作避免在UI线程执行

### 电量优化
- 蓝牙扫描采用间隔扫描策略
- 网络请求批量处理和压缩
- 后台任务优化和延迟执行
- 定位服务按需开启

### 启动优化
- 懒加载和按需初始化
- 启动任务优先级排序
- 减少冷启动时的网络请求
- 使用WorkManager处理非紧急任务

## 配置与环境

### 编译与打包
- 项目支持两种产品风格:
  - `production`: 正式发布版本
  - `demo`: 测试版本

### 环境配置
- 开发环境：`debug` 变体，启用日志记录和调试功能
- 测试环境：`staging` 变体，连接测试服务器
- 生产环境：`release` 变体，优化性能和安全性

### 版本控制
- 版本号格式: `majorVersion.minorVersion.patchVersion`
- 构建号基于Git提交数量生成
- 开发分支规范：
  - `main`: 主分支，稳定版本
  - `develop`: 开发分支
  - `feature/*`: 功能分支
  - `bugfix/*`: 问题修复分支
  - `release/*`: 发布准备分支

## 开发者快速上手

### 环境准备
1. 安装最新版Android Studio
2. 克隆代码仓库
3. 配置本地`keystore.properties`文件
4. 同步Gradle项目

### 构建与运行
1. 选择开发变体（通常为`debug`）
2. 连接测试设备或创建模拟器
3. 点击运行按钮或使用`./gradlew :app:installDebug`命令

### 添加新设备支持
1. 在`core_model`模块中定义设备模型
2. 在`lib_cmd`模块中实现设备通信协议
3. 在`app`模块中创建设备UI组件
4. 在设备管理器中注册新设备类型
5. 添加相应的单元测试和UI测试

### 调试技巧
- 使用Timber日志跟踪问题
- 开启严格模式检测性能问题
- 使用Android Profiler分析性能瓶颈
- 启用蓝牙HCI日志分析蓝牙通信问题

## 贡献与开发

### 开发规范
- 遵循Kotlin编码规范
- 使用MVVM架构模式
- 异步操作优先使用协程
- 保持模块间低耦合
- 代码提交前进行单元测试
- 提交信息格式：`<type>(<scope>): <description>`

### 问题反馈
如发现Bug或有功能建议，请通过以下方式反馈：
- 提交Issue到项目仓库
- 发送邮件至支持团队
- 在应用内使用"反馈"功能

### 重要说明
- 项目需要签名配置，详见keystore.properties文件
- 部分模块依赖特定第三方SDK，请确保依赖完整
- 首次构建可能需要较长时间下载依赖
- 使用VPN可能导致Gradle同步问题
