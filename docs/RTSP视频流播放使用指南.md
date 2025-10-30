# RTSP 视频流播放使用指南

## 功能概述

本文档介绍如何使用新增的 RTSP 视频流播放功能。该功能基于 ExoPlayer (Media3) 实现，支持播放网关设备上摄像头的实时视频流。

## 架构设计

### 模块结构

```
mCloudapp_V5/
├── lib_rtsp/                      # RTSP 播放器模块（新增）
│   ├── player/
│   │   ├── RtspPlayer.kt         # 播放器核心类
│   │   ├── RtspPlayerState.kt    # 播放器状态定义
│   │   └── RtspPlayerListener.kt # 播放器事件监听
│   ├── RtspManager.kt             # 播放器管理器
│   └── koin/
│       └── LibRtspKoinModule.kt  # Koin 依赖注入配置
│
└── app/
    ├── ui/
    │   ├── page/rtsp/
    │   │   └── RtspVideoActivity.kt  # 视频播放页面
    │   └── viewmodel/
    │       └── RtspViewModel.kt       # 播放器 ViewModel
    └── res/layout/
        └── activity_rtsp_video.xml   # 视频播放布局
```

### 技术栈

- **ExoPlayer (Media3)**: Google 官方视频播放器，原生支持 RTSP 协议
- **Kotlin Coroutines**: 异步处理和状态管理
- **StateFlow**: 响应式状态流
- **Koin**: 依赖注入框架
- **MVVM**: 架构模式

## 快速开始

### 1. 从其他页面跳转到视频播放页面

```kotlin
// 方式一：仅传入 RTSP 地址
val intent = Intent(this, RtspVideoActivity::class.java)
intent.putExtra(RtspVideoActivity.EXTRA_RTSP_URL, "rtsp://192.168.1.100:8554/camera")
startActivity(intent)

// 方式二：传入 RTSP 地址和摄像头名称
val intent = Intent(this, RtspVideoActivity::class.java)
intent.putExtra(RtspVideoActivity.EXTRA_RTSP_URL, "rtsp://192.168.1.100:8554/camera")
intent.putExtra(RtspVideoActivity.EXTRA_CAMERA_NAME, "摄像头1")
startActivity(intent)
```

### 2. 在自定义页面中使用播放器

如果需要在自己的页面中集成播放器，可以参考以下步骤：

#### 步骤 1：在 ViewModel 中注入 RtspManager

```kotlin
class YourViewModel(
    private val rtspManager: RtspManager
) : ViewModel() {
    
    fun playStream(rtspUrl: String) {
        rtspManager.playStream(rtspUrl)
    }
    
    fun pause() {
        rtspManager.pause()
    }
    
    // ... 其他控制方法
}
```

#### 步骤 2：在 Activity/Fragment 中绑定播放器

```kotlin
class YourActivity : BaseActivity() {
    
    private val viewModel: YourViewModel by viewModel()
    
    override fun initView(savedInstanceState: Bundle?) {
        // 绑定播放器到 PlayerView
        viewModel.getPlayer().attachToPlayerView(binding.playerView)
        
        // 或者绑定到 SurfaceView（自定义 UI）
        // viewModel.getPlayer().attachToSurfaceView(binding.surfaceView)
    }
    
    override fun createObserver() {
        // 观察播放器状态
        lifecycleScope.launch {
            viewModel.rtspManager.stateFlow.collect { result ->
                when (result) {
                    is RtspManagerResult.PlayerStateChanged -> {
                        handlePlayerState(result.state)
                    }
                    is RtspManagerResult.VideoSizeChanged -> {
                        // 处理视频尺寸变化
                    }
                    is RtspManagerResult.ProgressUpdate -> {
                        // 处理播放进度更新
                    }
                }
            }
        }
    }
}
```

#### 步骤 3：布局文件中添加 PlayerView

```xml
<androidx.media3.ui.PlayerView
    android:id="@+id/playerView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:show_buffering="when_playing"
    app:use_controller="true" />
```

## RTSP 地址格式

### 标准格式

```
rtsp://[用户名:密码@]IP地址:端口/流路径
```

### 示例

```
# 无认证
rtsp://192.168.1.100:8554/camera

# 带认证
rtsp://admin:password@192.168.1.100:8554/camera

# 多路摄像头
rtsp://192.168.1.100:8554/camera1
rtsp://192.168.1.100:8554/camera2
rtsp://192.168.1.100:8554/camera3
```

## API 说明

### RtspManager

播放器管理器，提供播放控制接口。

```kotlin
class RtspManager(context: Context) {
    
    // 获取播放器实例
    fun getPlayer(): RtspPlayer
    
    // 播放 RTSP 流
    fun playStream(rtspUrl: String)
    
    // 暂停播放
    fun pause()
    
    // 恢复播放
    fun resume()
    
    // 停止播放
    fun stop()
    
    // 释放播放器资源
    fun release()
    
    // 状态流（用于订阅状态变化）
    val stateFlow: SharedFlow<RtspManagerResult>
}
```

### RtspPlayer

播放器核心类，封装了 ExoPlayer。

```kotlin
class RtspPlayer(context: Context) {
    
    // 初始化播放器
    fun initialize()
    
    // 设置事件监听器
    fun setListener(listener: RtspPlayerListener)
    
    // 绑定到 PlayerView
    fun attachToPlayerView(playerView: PlayerView)
    
    // 绑定到 SurfaceView
    fun attachToSurfaceView(surfaceView: SurfaceView)
    
    // 播放 RTSP 流
    fun playRtspStream(rtspUrl: String)
    
    // 暂停播放
    fun pause()
    
    // 恢复播放
    fun resume()
    
    // 停止播放
    fun stop()
    
    // 跳转到指定位置（录播视频）
    fun seekTo(positionMs: Long)
    
    // 获取当前播放位置
    fun getCurrentPosition(): Long
    
    // 获取视频总时长
    fun getDuration(): Long
    
    // 判断是否正在播放
    fun isPlaying(): Boolean
    
    // 释放播放器资源
    fun release()
}
```

### RtspPlayerState

播放器状态定义。

```kotlin
sealed class RtspPlayerState {
    data object Idle : RtspPlayerState()                    // 空闲
    data class Buffering(val percent: Int) : RtspPlayerState()  // 缓冲中
    data object Ready : RtspPlayerState()                   // 准备就绪
    data object Playing : RtspPlayerState()                 // 播放中
    data object Paused : RtspPlayerState()                  // 已暂停
    data object Ended : RtspPlayerState()                   // 播放结束
    data class Error(val message: String, val exception: Exception?) : RtspPlayerState()  // 错误
}
```

## 常见问题

### 1. 无法连接到 RTSP 服务器

**可能原因：**
- RTSP 地址错误
- 网络不通
- 防火墙阻止
- 服务器未启动

**解决方法：**
- 检查 RTSP 地址是否正确
- 确保手机和网关在同一网络
- 使用 VLC 播放器测试 RTSP 地址是否可用

### 2. 视频画面卡顿

**可能原因：**
- 网络带宽不足
- 视频码率过高
- 设备性能不足

**解决方法：**
- 降低视频码率
- 使用 RTSP over TCP（更稳定但延迟稍高）
- 减少同时播放的摄像头数量

### 3. 播放延迟过高

**可能原因：**
- 缓冲时间设置过长
- 网络延迟

**优化方法：**
可以在 `RtspPlayer.kt` 中调整缓冲参数：

```kotlin
exoPlayer = ExoPlayer.Builder(context)
    .setLoadControl(
        DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                1000,  // 最小缓冲时间 1秒
                2000,  // 最大缓冲时间 2秒
                500,   // 播放开始缓冲时间
                1000   // 重新缓冲时间
            )
            .build()
    )
    .build()
```

### 4. 应用退出后仍在播放

确保在 Activity 的生命周期方法中正确处理：

```kotlin
override fun onPause() {
    super.onPause()
    viewModel.pause()  // 暂停播放
}

override fun onDestroy() {
    super.onDestroy()
    viewModel.stop()   // 停止播放
}
```

## 性能优化建议

### 1. 单路播放

对于单个摄像头的实时监控，直接使用 `RtspVideoActivity` 即可。

### 2. 多路播放

如需同时显示多路摄像头画面，建议：
- 最多同时播放 4 路视频
- 使用 RecyclerView + ViewHolder 复用机制
- 在 ViewHolder 中创建独立的播放器实例

### 3. 内存管理

- 及时释放不使用的播放器
- 避免创建过多播放器实例
- 监控内存使用情况

## 测试

### 搭建测试 RTSP 服务器

#### 方式一：使用 mediamtx（推荐）

```bash
# 使用 Docker 快速启动
docker run --rm -it -p 8554:8554 aler9/rtsp-simple-server

# 使用 FFmpeg 推流测试
ffmpeg -re -i test.mp4 -c copy -f rtsp rtsp://localhost:8554/test
```

#### 方式二：使用 VLC

1. 打开 VLC 播放器
2. 媒体 -> 流 -> 添加视频文件
3. 选择 RTSP 协议输出
4. 设置端口和路径

## 未来扩展

### 计划功能

- [ ] 支持录制视频到本地
- [ ] 支持截图功能
- [ ] 支持视频回放（录播）
- [ ] 支持多路画面分割显示
- [ ] 支持云台控制（PTZ）
- [ ] 支持对讲功能
- [ ] 支持视频质量自适应

## 联系方式

如有问题或建议，请联系开发团队。

---

**文档版本**: 1.0  
**最后更新**: 2025-10-30  
**作者**: gonghe

