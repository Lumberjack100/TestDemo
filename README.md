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

## 技术架构

### 开发环境
- **编程语言**: Kotlin 
- **构建工具**: Gradle 8.x
- **最低支持Android版本**: 见 `libs.versions.minSdk`
- **目标Android版本**: 见 `libs.versions.targetSdk`

### 主要架构与模式
- **MVVM 架构**: 使用 ViewModel 和 LiveData/StateFlow 实现数据绑定和UI更新
- **模块化结构**: 将功能按照职责划分为不同模块，降低耦合度
- **Repository 模式**: 统一数据访问接口，抽象数据源
- **依赖注入**: 使用 Koin 实现依赖注入
- **协程(Coroutines)**: 优先使用协程和 async/await 模式处理异步操作

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

## 功能特性

### 核心功能
- **设备连接与管理**: 支持BLE、TCP和云端API方式连接各类设备
- **设备配置**: 提供友好的界面进行设备参数配置
- **实时数据监控**: 可视化展示设备状态和传感器数据
- **数据分析**: 历史数据查询
- **固件升级**: OTA方式更新设备固件

### 支持的设备类型
- MR702 遥测终端机
- M50一体化 GNSS 监测站
- 一体式多参数裂缝计(LR200)
- 一体化雷达水位计
- 自组网报警网关
- ADME 自动化测斜机器人
- DAS设备
- 遥测终端倾斜仪(MR501)
- BHY-3S设备

## 项目结构

### 模块说明
- **app**: 应用主模块，包含UI界面和应用逻辑
- **core_data**: 数据层，负责本地存储和远程数据获取
- **core_model**: 定义实体模型类
- **core_commonlib**: 通用工具类和基础组件
- **lib_ble**: 蓝牙通信模块，处理蓝牙设备连接和通信
- **lib_cmd**: 设备指令处理库
- **lib_tcp**: TCP通信模块，处理网络设备连接和通信
- **lib_network**: 网络请求库

### 主要目录说明
```
mCloudapp_V5/
├── app/                       # 应用主模块
│   └── src/main/
│       ├── assets/           # 静态资源文件
│       ├── java/com/shmedo/mcloudapp/
│       │   ├── ui/           # 界面相关类
│       │   ├── model/        # 应用级数据模型
│       │   ├── utils/        # 工具类
│       │   ├── extensions/   # Kotlin扩展函数
│       │   ├── koin/         # 依赖注入配置
│       │   └── MCloudApplication.kt # 应用入口类
│       └── res/              # 资源文件
├── core_data/                 # 数据处理模块
│   └── src/main/java
├── core_model/                # 数据模型定义
│   └── src/main/java/com/shmedo/core/model/
│       ├── iotmodel/         # IoT设备模型
│       └── enums/            # 枚举类型
├── core_commonlib/            # 通用库
├── lib_ble/                   # 蓝牙通信
├── lib_cmd/                   # 命令处理
├── lib_tcp/                   # TCP通信
└── lib_network/               # 网络请求
```

## 主要流程与模式

### 设备连接流程
1. 通过蓝牙扫描/TCP连接/云端API获取设备信息
2. 建立通信连接
3. 发送认证信息
4. 建立会话并维持心跳

### 数据处理流程
1. 从设备获取原始数据
2. 根据设备类型和传感器模型解析数据
3. 存储到本地数据库
4. UI层通过观察LiveData/StateFlow获取并显示数据

## 配置与环境

### 编译与打包
- 项目支持两种产品风格:
  - `production`: 正式发布版本
  - `demo`: 测试版本

### 版本控制
- 版本号格式: `majorVersion.minorVersion.patchVersion`
- 构建号基于Git提交数量生成

## 贡献与开发

### 开发规范
- 遵循Kotlin编码规范
- 使用MVVM架构模式
- 异步操作优先使用协程
- 保持模块间低耦合
- 代码提交前进行单元测试

### 重要说明
- 项目需要签名配置，详见keystore.properties文件
- 部分模块依赖特定第三方SDK，请确保依赖完整
