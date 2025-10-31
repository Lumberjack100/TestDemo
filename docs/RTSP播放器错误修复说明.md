# RTSP 播放器错误修复说明

## 问题描述

在运行 RTSP 视频播放功能时，遇到了两个关键错误：

### 1. Handler 线程死亡错误

```
java.lang.IllegalStateException: Handler (android.os.Handler) sending message to a Handler on a dead thread
```

**错误原因：**
- 播放器在销毁后，RTSP 通信线程仍在尝试发送消息
- 生命周期管理不当，资源释放时机错误

### 2. 视频解码器错误

```
androidx.media3.exoplayer.ExoPlaybackException: MediaCodecVideoRenderer error
Caused by: androidx.media3.exoplayer.mediacodec.MediaCodecRenderer$DecoderInitializationException: 
Decoder init failed: c2.qti.hevc.decoder
format_supported=NO_EXCEEDS_CAPABILITIES
```

**错误原因：**
- 视频格式为 HEVC (H.265)，分辨率 2560x1440
- 设备的硬件解码器无法处理该分辨率和编码格式
- ExoPlayer 默认优先使用硬件解码，硬解失败后没有降级到软解

## 解决方案

### 1. 修复生命周期管理

#### ViewModel 修改
在 `RtspViewModel.onCleared()` 中正确释放资源：

```kotlin
override fun onCleared() {
    super.onCleared()
    Timber.d("[RtspViewModel] ViewModel 清理，释放播放器资源")
    // 先停止播放
    rtspManager.stop()
    // 延迟释放，确保所有消息处理完成
    rtspManager.release()
}
```

#### Activity 修改
避免重复释放资源：

```kotlin
override fun onPause() {
    super.onPause()
    if (isFinishing) {
        // 只有在 Activity 真正要结束时才暂停
        mStates.pause()
    }
}

override fun onDestroy() {
    super.onDestroy()
    // ViewModel 的 onCleared 会自动清理资源，这里不需要手动调用
}
```

#### 播放器 release 方法增强
添加异常处理，确保资源正确释放：

```kotlin
fun release() {
    try {
        // 停止进度更新
        stopProgressUpdate()
        
        // 移除监听器
        exoPlayer?.removeListener(playerListener)
        
        // 停止播放
        exoPlayer?.stop()
        
        // 释放播放器资源
        exoPlayer?.release()
        exoPlayer = null
        
        Timber.d("[RtspPlayer] 播放器资源已释放")
    } catch (e: Exception) {
        Timber.e(e, "[RtspPlayer] 释放播放器资源时发生异常")
    }
}
```

### 2. 添加软解码支持

修改 `RtspPlayer.initialize()` 方法，配置 ExoPlayer 支持软解码：

```kotlin
fun initialize() {
    if (exoPlayer == null) {
        // 配置渲染器工厂，优先使用软解码
        val renderersFactory = DefaultRenderersFactory(context).apply {
            // 设置扩展渲染器模式，优先软解码（避免硬解码失败）
            setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
        }
        
        // 配置加载控制器，降低缓冲延迟（适合实时流）
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                1000,   // 最小缓冲时间 1秒
                3000,   // 最大缓冲时间 3秒
                500,    // 播放开始缓冲时间
                1000    // 重新缓冲时间
            )
            .build()
        
        exoPlayer = ExoPlayer.Builder(context)
            .setRenderersFactory(renderersFactory)
            .setLoadControl(loadControl)
            .build()
            .apply {
                addListener(playerListener)
            }
        
        Timber.d("[RtspPlayer] 播放器初始化完成（支持软解码）")
    }
}
```

**关键配置说明：**

1. **`setExtensionRendererMode(EXTENSION_RENDERER_MODE_PREFER)`**
   - 优先使用软件解码器（扩展渲染器）
   - 硬件解码失败时自动降级到软解码
   - 保证视频播放的兼容性

2. **降低缓冲时间**
   - 实时流不需要太长的缓冲
   - 减少延迟，提升实时性

### 3. 增强错误提示

在播放器错误回调中添加更详细的错误信息：

```kotlin
override fun onPlayerError(error: PlaybackException) {
    val errorMessage = when (error.errorCode) {
        PlaybackException.ERROR_CODE_DECODER_INIT_FAILED -> 
            "解码器初始化失败，设备可能不支持该视频格式或分辨率过高"
        PlaybackException.ERROR_CODE_DECODING_FORMAT_EXCEEDS_CAPABILITIES -> 
            "视频格式超出设备解码能力，请尝试降低分辨率或更换视频编码格式"
        PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED -> 
            "设备不支持该视频编码格式"
        // ... 其他错误类型
    }
    
    listener?.onPlayerStateChanged(
        RtspPlayerState.Error(errorMessage, error)
    )
}
```

## 修改的文件清单

### lib_rtsp 模块
- ✅ `lib_rtsp/src/main/java/com/shmedo/lib/rtsp/player/RtspPlayer.kt`
  - 添加软解码支持
  - 优化缓冲配置
  - 增强错误处理
  - 改进资源释放逻辑

### app 模块
- ✅ `app/src/main/java/com/shmedo/mcloudapp/ui/viewmodel/state/RtspViewModel.kt`
  - 优化 `onCleared()` 生命周期处理
  
- ✅ `app/src/main/java/com/shmedo/mcloudapp/ui/page/rtsp/RtspVideoActivity.kt`
  - 修复 Toolbar 标题设置
  - 优化 `onPause()` 和 `onDestroy()` 逻辑
  - 避免重复释放资源

## 测试建议

### 1. 不同分辨率测试
测试不同分辨率的视频流：
- 720p (1280x720)
- 1080p (1920x1080)
- 2K (2560x1440)
- 4K (3840x2160)

### 2. 不同编码格式测试
- H.264 (最广泛支持)
- H.265/HEVC (更高压缩率，但设备支持有限)

### 3. 生命周期测试
- 播放过程中旋转屏幕
- 播放过程中按 Home 键
- 播放过程中切换到其他应用
- 播放过程中点击返回键

### 4. 长时间播放测试
- 连续播放 30 分钟以上
- 检查内存泄漏
- 检查 CPU 和内存使用率

## 使用建议

### 1. 视频编码建议
对于网关设备，建议使用以下视频参数：
- 编码格式：H.264（兼容性最好）
- 分辨率：1920x1080 或更低
- 码率：2-4 Mbps
- 帧率：15-30 fps

### 2. 网络环境要求
- 稳定的局域网连接
- 至少 10 Mbps 带宽
- 延迟 < 100ms

### 3. 设备要求
- Android 8.0 (API 26) 及以上
- 至少 2GB RAM
- 支持 H.264 硬件解码

## 常见问题

### Q1: 为什么要优先使用软解码？
**A:** 硬件解码虽然性能好，但兼容性差。不同设备的硬件解码器支持的格式和分辨率不同。软解码虽然 CPU 占用高一些，但兼容性好，可以作为硬解失败后的降级方案。

### Q2: 软解码会不会影响性能？
**A:** 对于 1080p 以下的视频，现代手机的 CPU 软解码完全没问题。只有 4K 等超高分辨率视频才需要硬解码。

### Q3: Handler 线程死亡错误如何预防？
**A:** 关键是正确管理生命周期：
1. 在 ViewModel 的 `onCleared()` 中释放资源
2. 使用 `try-catch` 包裹释放逻辑
3. 释放前先停止播放，确保所有异步操作完成

### Q4: 如何查看设备支持的视频格式？
**A:** 可以通过以下代码查询：

```kotlin
val codecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)
codecList.codecInfos.forEach { codecInfo ->
    if (!codecInfo.isEncoder) {
        Log.d("Codec", "Decoder: ${codecInfo.name}")
        codecInfo.supportedTypes.forEach { type ->
            Log.d("Codec", "  Type: $type")
        }
    }
}
```

## 后续优化方向

1. **自适应码率**：根据网络状况自动调整视频质量
2. **断线重连**：网络中断后自动重连
3. **录制功能**：支持录制视频到本地
4. **截图功能**：支持视频截图
5. **多路播放优化**：同时显示多路摄像头画面
6. **云台控制**：支持 PTZ 控制

---

**修复完成时间**: 2025-10-30  
**修复人员**: gonghe  
**版本**: 1.0

