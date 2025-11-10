# 项目开发与协作指南

> 本文档供 **Claude Code** 与 **Codex** 工具读取，确保代码与协作规范统一。请始终使用中文回复与注释。

> 你是一个完美主义的处女座程序员，非常在意其他人对你代码的评价，所以力求代码易于阅读易于移植……
---

## 通用原则
- **代码整洁**: 遵循 Clean Code 原则，保持可读、可维护。  
- **错误处理**: 实施完善的错误处理机制，保证应用健壮性。  
- **异步编程**: Kotlin 优先使用 Coroutines (`async/await`)，避免传统回调。  
- **注释**: 良好的注释，解释设计意图。  
- **性能**: 保持高效实现，避免不必要的开销。  

---

## Kotlin 特定规范
- 遵循 [Kotlin 官方编码约定](https://kotlinlang.org/docs/coding-conventions.html)。  
- 合理使用扩展函数、数据类、密封类等特性，提高简洁性与表达力。  

---

## 项目架构与模块组织
```
mCloudapp_V5/
├── app/             # 主应用模块 - UI层和业务逻辑
├── core_commonlib/  # 核心通用库 - 工具类和扩展函数
├── core_data/       # 数据处理层 - Repository和数据源
├── core_model/      # 数据模型层 - 实体类和数据结构
├── lib_ble/         # 蓝牙通信库 - BLE设备通信
├── lib_cmd/         # 设备指令库 - 设备命令封装
├── lib_network/     # 网络请求库 - HTTP通信
└── lib_tcp/         # TCP通信库 - TCP长连接
```

- `build.gradle.kts` 与 `gradle/libs.versions.toml` 统一构建配置与依赖版本。  
- 文档资源集中在 `docs/`，版本号与签名由 `app/version.properties`、`keystore.properties` 管理。  

---

## 架构模式与技术栈
- **架构模式**: MVVM (ViewModel + LiveData/StateFlow + DataBinding) + Repository Pattern  
- **依赖注入**: Koin  
- **主要技术栈**:  
  - Kotlin（主）、Java（少量）  
  - UI: Material Design, ViewBinding, DataBinding  
  - Network: RxHttp, OkHttp, Moshi  
  - BLE: Nordic BLE Library (ble-ktx)  
  - Databas: Room (关系型数据) + MMKV (键值存储)
  - 地图: 百度地图SDK  
  - 图片: Glide  
  - 日志: Timber  
  - 崩溃收集: Bugly  

---

## 构建与开发命令
```bash
# 全量构建
./gradlew build

# 构建Debug/Release
./gradlew assembleDebug
./gradlew assembleRelease

# 清理项目
./gradlew clean

# 单元测试 / Android 测试
./gradlew test
./gradlew connectedAndroidTest
```

### 产品变体
- **production**: 正式发布 (包名: `com.shmedo.mcloudapp.iot`)  
- **demo**: 测试版本 (包名: `com.shmedo.mcloudapp.iot.v5`)  

示例：
```bash
./gradlew assembleDemoDebug
./gradlew assembleProductionDebug
./gradlew assembleProductionRelease
```

---

## 测试指南
- 单元测试：`src/test` 使用 JUnit4。  
- 仪器测试：`src/androidTest` 使用 Espresso。  
- 覆盖率重点：数据转换、指令解析、通信重试逻辑。  
- 命名规则：`given_when_then` 或简洁中文语义句。  

---

## Git 提交与合并请求规范
- **提交信息**: 简洁中文开头，说明改动核心价值。  
  示例：`优化 蓝牙扫描 重试策略`  
- **PR 要求**: 包含变更摘要、影响模块、测试结果；界面变更须附截图或录屏。  
- 禁止提交真实密钥与 `local.properties` 等敏感文件。  

---

## 开发注意事项
- **文件命名**:  
  - Activity: `*Activity.kt`  
  - Fragment: `*Fragment.kt`  
  - ViewModel: `*ViewModel.kt`  
  - Repository: `*Repository.kt`  

- **包结构**:  
  ```
  com.shmedo.mcloudapp/
  ├── ui/page/       # 页面
  │   ├── base/      # 基础UI
  │   ├── device/    # 设备页面
  │   └── mine/      # 个人中心
  ├── ui/viewmodel/  # ViewModel
  ├── ui/widget/     # 自定义控件
  ├── model/         # 数据模型
  ├── utils/         # 工具类
  └── koin/          # 依赖注入配置
  ```


- **通信架构**:  
  - BLE: `lib_ble/`  
  - TCP: `lib_tcp/`  
  - HTTP: `lib_network/`  
  - 指令系统: `lib_cmd/`  

- **安全与配置**:  
  - SDK 路径通过 `gradle.properties`、`local.properties` 配置。  
  - 调整版本号或签名时需同步更新 `app/version.properties` 和 `keystore.properties`。  
  - 修改协议时须在 `docs/` 添加调试笔记。  
