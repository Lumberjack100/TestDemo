package com.shmedo.lib.rtsp

import android.content.Context
import com.shmedo.lib.rtsp.player.RtspPlayer
import com.shmedo.lib.rtsp.player.RtspPlayerListener
import com.shmedo.lib.rtsp.player.RtspPlayerState
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2025/10/30
 * 描述：RTSP 管理器
 * 
 * 统一管理 RTSP 播放器实例，提供播放控制接口
 * 使用 SharedFlow 向上层（ViewModel）发送状态更新
 * 
 * 设计模式：单例模式（通过 Koin 注入）
 */
class RtspManager(private val context: Context) {
    
    /**
     * 状态流（内部可变）
     * 用于发送播放器状态变化、视频尺寸变化、进度更新等事件
     */
    private val _stateFlow = MutableSharedFlow<RtspManagerResult>(
        replay = 1, // 保留最新的一个状态，便于新订阅者获取当前状态
        onBufferOverflow = BufferOverflow.DROP_OLDEST // 缓冲区满时丢弃最旧的数据
    )
    
    /**
     * 状态流（外部只读）
     * 供 ViewModel 订阅使用
     */
    val stateFlow = _stateFlow.asSharedFlow()
    
    /**
     * 播放器实例
     * 延迟初始化，首次调用 getPlayer() 时创建
     */
    private var rtspPlayer: RtspPlayer? = null
    
    /**
     * 获取或创建播放器实例
     * @return RtspPlayer 实例
     */
    fun getPlayer(): RtspPlayer {
        if (rtspPlayer == null) {
            rtspPlayer = RtspPlayer(context).apply {
                // 初始化播放器
                initialize()
                // 设置事件监听器
                setListener(playerListener)
            }
            Timber.d("[RtspManager] 创建播放器实例")
        }
        return rtspPlayer!!
    }
    
    /**
     * 播放 RTSP 流
     * @param rtspUrl RTSP 地址，格式示例：rtsp://192.168.1.100:8554/stream
     */
    fun playStream(rtspUrl: String) {
        Timber.d("[RtspManager] 播放 RTSP 流: $rtspUrl")
        getPlayer().playRtspStream(rtspUrl)
    }
    
    /**
     * 暂停播放
     */
    fun pause() {
        rtspPlayer?.pause()
        Timber.d("[RtspManager] 暂停播放")
    }
    
    /**
     * 恢复播放
     */
    fun resume() {
        rtspPlayer?.resume()
        Timber.d("[RtspManager] 恢复播放")
    }
    
    /**
     * 停止播放
     */
    fun stop() {
        rtspPlayer?.stop()
        Timber.d("[RtspManager] 停止播放")
    }
    
    /**
     * 释放播放器资源
     * 应在不再需要播放器时调用，例如退出应用或切换到其他功能模块
     */
    fun release() {
        rtspPlayer?.release()
        rtspPlayer = null
        Timber.d("[RtspManager] 释放播放器资源")
    }
    
    /**
     * 播放器事件监听器实现
     * 将播放器事件转换为 RtspManagerResult 并通过 Flow 发送
     */
    private val playerListener = object : RtspPlayerListener {
        
        /**
         * 播放器状态变化
         */
        override fun onPlayerStateChanged(state: RtspPlayerState) {
            _stateFlow.tryEmit(RtspManagerResult.PlayerStateChanged(state))
            Timber.d("[RtspManager] 状态变化: $state")
        }
        
        /**
         * 视频尺寸变化
         */
        override fun onVideoSizeChanged(width: Int, height: Int) {
            _stateFlow.tryEmit(RtspManagerResult.VideoSizeChanged(width, height))
            Timber.d("[RtspManager] 视频尺寸: ${width}x${height}")
        }
        
        /**
         * 播放进度更新
         */
        override fun onProgressUpdate(position: Long, duration: Long) {
            _stateFlow.tryEmit(RtspManagerResult.ProgressUpdate(position, duration))
        }
    }
}

