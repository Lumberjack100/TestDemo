package com.shmedo.lib.rtsp

import com.shmedo.lib.rtsp.player.RtspPlayerState

/**
 * 创建者：gonghe
 * 创建时间：2025/10/30
 * 描述：RTSP 管理器状态结果封装
 * 
 * 封装播放器各种状态变化的结果，通过 Flow 向上层传递
 */
sealed class RtspManagerResult {
    
    /**
     * 播放器状态变化结果
     * @param state 新的播放器状态
     */
    data class PlayerStateChanged(val state: RtspPlayerState) : RtspManagerResult()
    
    /**
     * 视频尺寸变化结果
     * @param width 视频宽度（像素）
     * @param height 视频高度（像素）
     */
    data class VideoSizeChanged(val width: Int, val height: Int) : RtspManagerResult()
    
    /**
     * 播放进度更新结果
     * @param position 当前播放位置（毫秒）
     * @param duration 视频总时长（毫秒）
     */
    data class ProgressUpdate(val position: Long, val duration: Long) : RtspManagerResult()
}

