# Git 提交信息规范

## 提交格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

- **type**: 提交类型（必填）
- **scope**: 影响范围（可选）
- **subject**: 简短描述（必填，中文）
- **body**: 详细说明（可选）
- **footer**: 关联信息（可选）

---

## 提交类型速查

| 类型 | 说明 | 使用场景 |
|------|------|----------|
| `feat` | 新功能 | 添加新特性、新模块 |
| `fix` | Bug 修复 | 修复功能缺陷、崩溃问题 |
| `refactor` | 重构 | 代码结构优化，不改变功能 |
| `perf` | 性能优化 | 提升运行效率、减少资源消耗 |
| `style` | 代码格式 | 格式化、命名规范调整 |
| `docs` | 文档 | 更新文档、注释 |
| `test` | 测试 | 添加或修改测试代码 |
| `build` | 构建系统 | 修改 Gradle 配置、依赖版本 |
| `ci` | CI/CD | 修改构建流程、发布配置 |
| `chore` | 其他 | 工具配置、辅助脚本 |
| `revert` | 回滚 | 撤销之前的提交 |

---

## 提交示例

### 1. 新功能开发

```
feat(ble): 添加蓝牙设备自动重连机制

- 实现连接断开后自动重试逻辑
- 支持自定义重连间隔和次数
- 添加重连状态回调接口

影响模块: lib_ble
```

```
feat(tcp): 支持设备指令日志导出功能

- 新增日志筛选和导出界面
- 支持按时间范围和设备类型筛选
- 导出格式支持 CSV 和 TXT

影响文件:
- app/ui/page/device/CustomCommandLogPrintFragment.kt
- core_data/repository/DeviceLogRepository.kt
```

### 2. Bug 修复

```
fix(wifi): 修复 Android 13 WiFi 扫描权限崩溃

- 添加 NEARBY_WIFI_DEVICES 权限检查
- 兼容 Android 13+ 精准定位权限
- 优化权限请求流程

Closes #123
```

```
fix(rtsp): 修复视频播放器分辨率限制问题

- 移除硬编码的分辨率检查
- 支持自适应视频流分辨率
- 修复高分辨率视频黑屏问题
```

### 3. 重构优化

```
refactor(viewmodel): 统一设备页面 ViewModel 基类

- 抽取 BaseDeviceViewModel 公共逻辑
- 优化设备状态管理流程
- 简化各设备 ViewModel 实现

影响模块: app/ui/viewmodel
```

```
refactor(data): 迁移 Room 数据库到 core_data 模块

- 统一数据访问层架构
- 移除重复的数据库配置
- 更新模块依赖关系

影响模块: core_data, app
```

### 4. 性能优化

```
perf(list): 优化设备列表渲染性能

- 使用 DiffUtil 减少不必要的刷新
- 实现 ViewHolder 复用优化
- 列表滚动帧率提升 30%
```

### 5. 构建配置

```
build: 升级 Gradle 到 8.5 和 AGP 到 8.2.0

- 更新 gradle-wrapper.properties
- 适配新版本 API 变更
- 修复编译警告

Breaking Changes: 需要 Android Studio Hedgehog 以上版本
```

### 6. 依赖更新

```
chore(deps): 更新核心依赖库版本

- Kotlin 1.9.20 → 1.9.22
- Coroutines 1.7.3 → 1.8.0
- Room 2.6.0 → 2.6.1
- 修复已知安全漏洞

参考: gradle/libs.versions.toml
```

### 7. 版本发布

```
release: v5.2.0 正式版

新增功能:
- 支持设备群组管理
- 添加监测数据导出
- 优化蓝牙连接稳定性

问题修复:
- 修复 WiFi 配置失败问题
- 解决视频播放卡顿
- 修复内存泄漏

详见: CHANGELOG_PREVIEW.md
```

---

## 提交最佳实践

### 1. 简洁明确的主题行
- ✅ `fix(ble): 修复设备连接超时未释放资源`
- ❌ `修改了一些文件`

### 2. 善用 scope 标注范围
- 模块级别: `feat(lib_tcp)`, `fix(core_data)`
- 功能级别: `feat(login)`, `refactor(device-list)`
- 技术栈: `build(gradle)`, `chore(deps)`

### 3. Body 说明变更细节
- 说明为什么改（Why），而不仅是改了什么（What）
- 列出重要的技术决策和影响范围
- 对于复杂改动，提供上下文信息

### 4. Footer 关联追踪
- 关闭 Issue: `Closes #123` 或 `Fixes #456`
- 破坏性变更: `BREAKING CHANGE: 移除旧版 API`
- 关联文档: `参考: docs/架构设计说明.md`

### 5. 单一职责原则
- 一次提交只做一件事
- 功能开发和代码格式化分开提交
- Bug 修复和重构分开提交

---

## 多模块项目 Scope 参考

| Scope | 说明 |
|-------|------|
| `app` | 主应用模块 |
| `ble` | 蓝牙通信模块 (lib_ble) |
| `tcp` | TCP 通信模块 (lib_tcp) |
| `network` | 网络请求模块 (lib_network) |
| `rtsp` | RTSP 视频模块 (lib_rtsp) |
| `wifi` | WiFi 模块 (lib_wifi) |
| `cmd` | 设备指令模块 (lib_cmd) |
| `data` | 数据层模块 (core_data) |
| `model` | 数据模型模块 (core_model) |
| `common` | 通用工具库 (core_commonlib) |
| `deps` | 依赖管理 |
| `gradle` | 构建配置 |

---

**最后更新**: 2025-12-12
