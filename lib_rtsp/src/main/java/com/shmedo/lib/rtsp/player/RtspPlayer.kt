package com.shmedo.lib.rtsp.player

import android.content.Context
import android.view.SurfaceView
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2025/10/30
 * 描述：RTSP 播放器封装类
 *
 * 基于 ExoPlayer (Media3) 实现的 RTSP 视频流播放器
 * 支持实时流播放、播放控制、状态监听等功能
 */
class RtspPlayer(private val context: Context) {

    // ExoPlayer 实例
    private var exoPlayer: ExoPlayer? = null

    // 播放器事件监听器
    private var listener: RtspPlayerListener? = null

    // 进度更新协程任务
    private var progressJob: Job? = null

    // 协程作用域（主线程）
    private val scope = CoroutineScope(Dispatchers.Main)

    /**
     * 初始化播放器
     * 创建 ExoPlayer 实例并配置监听器
     */
    @OptIn(UnstableApi::class)
    fun initialize() {
        if (exoPlayer == null) {
            // 使用系统默认渲染器配置，避免额外解码扩展
            val renderersFactory = DefaultRenderersFactory(context).apply {
                setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
                // 允许略低兼容性的解码器参与
                setAllowedVideoJoiningTimeMs(5000)
                // 当硬解初始化失败时自动切换到软解
                setEnableDecoderFallback(true)
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
                    // 设置播放器事件监听
                    addListener(playerListener)
                }

            Timber.d("[RtspPlayer] 播放器初始化完成")
        }
    }
    
    /**
     * 设置播放器事件监听器
     * @param listener 事件监听器实现
     */
    fun setListener(listener: RtspPlayerListener) {
        this.listener = listener
    }
    
    /**
     * 绑定播放器到 PlayerView
     * PlayerView 提供完整的播放控制 UI
     * @param playerView Media3 的 PlayerView 组件
     */
    fun attachToPlayerView(playerView: PlayerView) {
        playerView.player = exoPlayer
        Timber.d("[RtspPlayer] 播放器已绑定到 PlayerView")
    }
    
    /**
     * 绑定播放器到 SurfaceView
     * 用于自定义 UI 的场景
     * @param surfaceView Android 原生 SurfaceView
     */
    fun attachToSurfaceView(surfaceView: SurfaceView) {
        exoPlayer?.setVideoSurfaceView(surfaceView)
        Timber.d("[RtspPlayer] 播放器已绑定到 SurfaceView")
    }
    
    /**
     * 设置 RTSP 流地址并开始播放
     * @param rtspUrl RTSP 地址，格式示例：rtsp://192.168.1.100:8554/stream
     */
    fun playRtspStream(rtspUrl: String) {
        try {
            // 创建媒体项
            val mediaItem = MediaItem.fromUri(rtspUrl)
            
            exoPlayer?.apply {
                // 设置媒体源
                setMediaItem(mediaItem)
                // 准备播放器（开始加载流）
                prepare()
                // 自动开始播放
                playWhenReady = true
            }
            
            Timber.d("[RtspPlayer] 开始播放 RTSP 流: $rtspUrl")
            
            // 启动播放进度更新任务
            startProgressUpdate()
            
        } catch (e: Exception) {
            Timber.e(e, "[RtspPlayer] 播放 RTSP 流失败")
            listener?.onPlayerStateChanged(
                RtspPlayerState.Error("播放失败: ${e.message}", e)
            )
        }
    }
    
    /**
     * 暂停播放
     * 保持网络连接，可快速恢复播放
     */
    fun pause() {
        exoPlayer?.pause()
        stopProgressUpdate()
        Timber.d("[RtspPlayer] 播放已暂停")
    }
    
    /**
     * 恢复播放
     * 从暂停状态继续播放
     */
    fun resume() {
        exoPlayer?.play()
        startProgressUpdate()
        Timber.d("[RtspPlayer] 播放已恢复")
    }
    
    /**
     * 停止播放
     * 断开网络连接，释放部分资源
     */
    fun stop() {
        exoPlayer?.stop()
        stopProgressUpdate()
        Timber.d("[RtspPlayer] 播放已停止")
    }
    
    /**
     * 跳转到指定播放位置
     * 主要用于录播视频，实时流不支持跳转
     * @param positionMs 目标位置（毫秒）
     */
    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
        Timber.d("[RtspPlayer] 跳转到位置: ${positionMs}ms")
    }
    
    /**
     * 获取当前播放位置
     * @return 当前播放位置（毫秒），实时流通常返回 0
     */
    fun getCurrentPosition(): Long {
        return exoPlayer?.currentPosition ?: 0L
    }
    
    /**
     * 获取视频总时长
     * @return 视频总时长（毫秒），实时流返回 0
     */
    fun getDuration(): Long {
        return exoPlayer?.duration ?: 0L
    }
    
    /**
     * 判断是否正在播放
     * @return true 表示正在播放，false 表示已暂停或停止
     */
    fun isPlaying(): Boolean {
        return exoPlayer?.isPlaying ?: false
    }
    
    /**
     * 释放播放器资源
     * 停止播放，释放所有资源，播放器实例将不可再用
     * 应在 Activity/Fragment 的 onDestroy 中调用
     */
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
    
    /**
     * 启动播放进度更新任务
     * 每 500ms 更新一次播放进度，用于进度条显示
     */
    private fun startProgressUpdate() {
        // 先停止之前的任务
        stopProgressUpdate()
        
        // 启动新的协程任务
        progressJob = scope.launch {
            while (isActive && exoPlayer?.isPlaying == true) {
                val position = getCurrentPosition()
                val duration = getDuration()
                listener?.onProgressUpdate(position, duration)
                delay(500) // 每 500ms 更新一次
            }
        }
    }
    
    /**
     * 停止播放进度更新任务
     */
    private fun stopProgressUpdate() {
        progressJob?.cancel()
        progressJob = null
    }
    
    /**
     * ExoPlayer 事件监听器
     * 将 ExoPlayer 的底层事件转换为业务层状态
     */
    private val playerListener = object : Player.Listener {
        
        /**
         * 播放状态变化回调
         */
        override fun onPlaybackStateChanged(playbackState: Int) {
            val state = when (playbackState) {
                Player.STATE_IDLE -> {
                    // 空闲状态：播放器刚创建或调用了 stop()
                    RtspPlayerState.Idle
                }
                Player.STATE_BUFFERING -> {
                    // 缓冲状态：正在加载数据
                    RtspPlayerState.Buffering(0)
                }
                Player.STATE_READY -> {
                    // 准备就绪：可以播放了
                    if (exoPlayer?.isPlaying == true) {
                        RtspPlayerState.Playing
                    } else {
                        RtspPlayerState.Ready
                    }
                }
                Player.STATE_ENDED -> {
                    // 播放结束（录播视频会到这个状态）
                    RtspPlayerState.Ended
                }
                else -> RtspPlayerState.Idle
            }
            
            listener?.onPlayerStateChanged(state)
            Timber.d("[RtspPlayer] 播放状态变化: $state")
        }
        
        /**
         * 播放/暂停状态变化回调
         */
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            val state = if (isPlaying) {
                RtspPlayerState.Playing
            } else {
                RtspPlayerState.Paused
            }
            listener?.onPlayerStateChanged(state)
            Timber.d("[RtspPlayer] 播放状态: ${if (isPlaying) "播放中" else "已暂停"}")
        }
        
        /**
         * 播放错误回调
         */
        override fun onPlayerError(error: PlaybackException) {
            Timber.e(error, "[RtspPlayer] 播放器错误")
            
            // 根据错误类型提供友好的错误信息
            val errorMessage = when (error.errorCode) {
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> 
                    "网络连接失败，请检查网络设置"
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> 
                    "网络连接超时，请检查 RTSP 地址是否正确"
                PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED -> 
                    "视频流格式错误"
                PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED -> 
                    "无法解析视频流"
                PlaybackException.ERROR_CODE_DECODER_INIT_FAILED -> 
                    "解码器初始化失败，设备可能不支持该视频格式或分辨率过高"
                PlaybackException.ERROR_CODE_DECODING_FORMAT_EXCEEDS_CAPABILITIES -> 
                    "视频格式超出设备解码能力，请尝试降低分辨率或更换视频编码格式"
                PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED -> 
                    "设备不支持该视频编码格式"
                else -> {
                    val msg = error.message ?: "播放器发生未知错误"
                    // 如果错误信息包含 MediaCodec，说明是解码相关问题
                    if (msg.contains("MediaCodec", ignoreCase = true)) {
                        "视频解码失败，设备可能不支持该视频格式（${error.errorCode}）"
                    } else {
                        msg
                    }
                }
            }
            
            listener?.onPlayerStateChanged(
                RtspPlayerState.Error(errorMessage, error)
            )
        }
        
        /**
         * 视频尺寸变化回调
         */
        override fun onVideoSizeChanged(videoSize: VideoSize) {
            listener?.onVideoSizeChanged(videoSize.width, videoSize.height)
            Timber.d("[RtspPlayer] 视频尺寸: ${videoSize.width}x${videoSize.height}")
        }
    }
}
