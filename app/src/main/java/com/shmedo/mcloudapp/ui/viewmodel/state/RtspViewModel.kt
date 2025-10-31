package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shmedo.lib.rtsp.RtspManager
import com.shmedo.lib.rtsp.RtspManagerResult
import com.shmedo.lib.rtsp.player.RtspPlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2025/10/30
 * 描述：RTSP 视频播放 ViewModel
 * 
 * 负责管理 RTSP 视频播放的业务逻辑和状态
 * 遵循 MVVM 架构模式，作为 View 和 Model 之间的桥梁
 */
class RtspViewModel(
    private val rtspManager: RtspManager
) : ViewModel() {
    
    /**
     * 播放器状态流（内部可变）
     */
    private val _playerState = MutableStateFlow<RtspPlayerState>(RtspPlayerState.Idle)
    
    /**
     * 播放器状态流（外部只读）
     * UI 层订阅此流以更新播放状态显示
     */
    val playerState: StateFlow<RtspPlayerState> = _playerState.asStateFlow()
    
    /**
     * 视频尺寸流（内部可变）
     */
    private val _videoSize = MutableStateFlow(Pair(0, 0))
    
    /**
     * 视频尺寸流（外部只读）
     * UI 层订阅此流以获取视频分辨率信息
     */
    val videoSize: StateFlow<Pair<Int, Int>> = _videoSize.asStateFlow()
    
    /**
     * 播放进度流（内部可变）
     * Pair<当前位置(ms), 总时长(ms)>
     */
    private val _progress = MutableStateFlow(Pair(0L, 0L))
    
    /**
     * 播放进度流（外部只读）
     * UI 层订阅此流以更新播放进度条
     */
    val progress: StateFlow<Pair<Long, Long>> = _progress.asStateFlow()
    
    init {
        // 初始化时开始观察 RTSP 管理器的状态变化
        observeRtspState()
    }
    
    /**
     * 观察 RTSP 状态变化
     * 订阅 RtspManager 的状态流，并更新 ViewModel 的状态
     */
    private fun observeRtspState() {
        viewModelScope.launch {
            rtspManager.stateFlow.collect { result ->
                when (result) {
                    is RtspManagerResult.PlayerStateChanged -> {
                        // 播放器状态变化
                        _playerState.value = result.state
                        Timber.d("[RtspViewModel] 播放器状态: ${result.state}")
                    }
                    is RtspManagerResult.VideoSizeChanged -> {
                        // 视频尺寸变化
                        _videoSize.value = Pair(result.width, result.height)
                        Timber.d("[RtspViewModel] 视频尺寸: ${result.width}x${result.height}")
                    }
                    is RtspManagerResult.ProgressUpdate -> {
                        // 播放进度更新
                        _progress.value = Pair(result.position, result.duration)
                    }
                }
            }
        }
    }
    
    /**
     * 播放 RTSP 流
     * @param rtspUrl RTSP 地址，格式示例：rtsp://192.168.1.100:8554/stream
     */
    fun playStream(rtspUrl: String) {
        Timber.d("[RtspViewModel] 请求播放: $rtspUrl")
        rtspManager.playStream(rtspUrl)
    }
    
    /**
     * 暂停播放
     */
    fun pause() {
        Timber.d("[RtspViewModel] 请求暂停")
        rtspManager.pause()
    }
    
    /**
     * 恢复播放
     */
    fun resume() {
        Timber.d("[RtspViewModel] 请求恢复")
        rtspManager.resume()
    }
    
    /**
     * 停止播放
     */
    fun stop() {
        Timber.d("[RtspViewModel] 请求停止")
        rtspManager.stop()
    }
    
    /**
     * 获取播放器实例
     * 用于 Activity/Fragment 绑定播放器到 View
     * @return RtspPlayer 实例
     */
    fun getPlayer() = rtspManager.getPlayer()
    
    /**
     * ViewModel 清理时调用
     * 释放播放器资源，避免内存泄漏
     */
    override fun onCleared() {
        super.onCleared()
        Timber.d("[RtspViewModel] ViewModel 清理，释放播放器资源")
        // 先停止播放
        rtspManager.stop()
        // 延迟释放，确保所有消息处理完成
        rtspManager.release()
    }
}
