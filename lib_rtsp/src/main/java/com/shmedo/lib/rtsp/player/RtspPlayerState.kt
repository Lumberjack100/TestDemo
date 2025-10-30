package com.shmedo.lib.rtsp.player

/**
 * 创建者：gonghe
 * 创建时间：2025/10/30
 * 描述：RTSP 播放器状态定义
 * 
 * 该密封类定义了播放器的所有可能状态，用于状态管理和 UI 更新
 */
sealed class RtspPlayerState {
    
    /**
     * 空闲状态
     * 播放器已创建但未开始播放
     */
    data object Idle : RtspPlayerState()
    
    /**
     * 缓冲中状态
     * @param percent 缓冲进度百分比（0-100）
     */
    data class Buffering(val percent: Int) : RtspPlayerState()
    
    /**
     * 准备就绪状态
     * 播放器已准备好，可以开始播放
     */
    data object Ready : RtspPlayerState()
    
    /**
     * 正在播放状态
     * 视频流正在播放中
     */
    data object Playing : RtspPlayerState()
    
    /**
     * 已暂停状态
     * 播放已暂停，可以恢复
     */
    data object Paused : RtspPlayerState()
    
    /**
     * 播放结束状态
     * 视频播放完毕（通常用于录播视频）
     */
    data object Ended : RtspPlayerState()
    
    /**
     * 错误状态
     * @param message 错误描述信息
     * @param exception 异常对象（可选）
     */
    data class Error(val message: String, val exception: Exception?) : RtspPlayerState()
}

