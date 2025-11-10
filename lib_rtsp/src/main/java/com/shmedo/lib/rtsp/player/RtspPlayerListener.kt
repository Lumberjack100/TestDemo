package com.shmedo.lib.rtsp.player

/**
 * 创建者：gonghe
 * 创建时间：2025/10/30
 * 描述：RTSP 播放器事件监听接口
 * 
 * 定义播放器各种事件的回调接口，用于通知外部播放状态变化
 */
interface RtspPlayerListener {
    
    /**
     * 播放器状态变化回调
     * @param state 新的播放器状态
     */
    fun onPlayerStateChanged(state: RtspPlayerState)
    
    /**
     * 视频尺寸变化回调
     * 当视频流的分辨率信息可用或变化时触发
     * @param width 视频宽度（像素）
     * @param height 视频高度（像素）
     */
    fun onVideoSizeChanged(width: Int, height: Int)
    
    /**
     * 播放进度更新回调（可选实现）
     * 用于录播视频的进度条更新，实时流可忽略此回调
     * @param position 当前播放位置（毫秒）
     * @param duration 视频总时长（毫秒），实时流返回 0
     */
    fun onProgressUpdate(position: Long, duration: Long) {}
}

