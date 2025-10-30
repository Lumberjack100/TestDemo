package com.shmedo.mcloudapp.ui.page.rtsp

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ToastUtils
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.rtsp.player.RtspPlayerState
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.ActivityRtspVideoBinding
import com.shmedo.mcloudapp.ui.page.base.activity.BaseActivity
import com.shmedo.mcloudapp.ui.viewmodel.state.RtspViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

/**
 * 创建者：gonghe
 * 创建时间：2025/10/30
 * 描述：RTSP 视频播放页面
 * 
 * 功能：
 * 1. 显示 RTSP 视频流
 * 2. 提供播放控制（播放、暂停、恢复、停止）
 * 3. 显示播放状态和视频信息
 * 4. 支持手动输入 RTSP 地址
 */
class RtspVideoActivity : BaseActivity() {
    private lateinit var binding: ActivityRtspVideoBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    // ViewModel 实例（通过 Koin 注入）
    private val mStates: RtspViewModel by viewModel()

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.activity_rtsp_video, BR.toolbarVM, toolbarViewModel)
            .addBindingParam(BR.click, BaseClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as ActivityRtspVideoBinding
        setToolBar(binding.llToolbar.toolbar)
        binding.llToolbar.toolbar.title = "发生错误"
        binding.llToolbar.toolbar.setNavigationOnClickListener {
            finish()
        }
        initPlayer()
    }

    private fun initPlayer() {
        // 绑定播放器到 PlayerView
        mStates.getPlayer().attachToPlayerView(binding.playerView)

        // 设置按钮点击事件
        setupClickListeners()
    }

    
    /**
     * 初始化数据
     */
    override fun initData() {
        // 从 Intent 获取 RTSP 地址（如果有）
        intent.getStringExtra(EXTRA_RTSP_URL)?.let { url ->
            binding.etRtspUrl.setText(url)
            Timber.d("[RtspVideoActivity] 从 Intent 获取 RTSP 地址: $url")
        }
        
        // 从 Intent 获取摄像头名称（如果有）
        intent.getStringExtra(EXTRA_CAMERA_NAME)?.let { name ->
            supportActionBar?.title = name
            Timber.d("[RtspVideoActivity] 摄像头名称: $name")
        }
    }
    
    /**
     * 创建数据观察者
     */
    override fun createObserver() {
        // 观察播放器状态
        lifecycleScope.launch {
            mStates.playerState.collect { state ->
                handlePlayerState(state)
            }
        }
        
        // 观察视频尺寸
        lifecycleScope.launch {
            mStates.videoSize.collect { (width, height) ->
                if (width > 0 && height > 0) {
                    val info = getString(R.string.rtsp_video_resolution, width, height)
                    binding.tvVideoInfo.text = info
                    Timber.d("[RtspVideoActivity] 视频尺寸: ${width}x${height}")
                }
            }
        }
        
        // 观察播放进度（可选，用于调试）
        lifecycleScope.launch {
            mStates.progress.collect { (position, duration) ->
                // 实时流的 duration 通常为 0，此处仅用于调试
                if (duration > 0) {
                    Timber.v("[RtspVideoActivity] 播放进度: $position / $duration")
                }
            }
        }
    }
    
    /**
     * 设置按钮点击事件
     */
    private fun setupClickListeners() {
        // 播放按钮
        binding.btnPlay.setOnClickListener {
            val rtspUrl = binding.etRtspUrl.text.toString().trim()
            
            if (rtspUrl.isEmpty()) {
                ToastUtils.showShort(R.string.rtsp_error_empty_url)
                return@setOnClickListener
            }
            
            if (!rtspUrl.startsWith("rtsp://")) {
                ToastUtils.showShort(R.string.rtsp_error_invalid_url)
                return@setOnClickListener
            }
            
            Timber.d("[RtspVideoActivity] 开始播放: $rtspUrl")
            mStates.playStream(rtspUrl)
        }
        
        // 暂停按钮
        binding.btnPause.setOnClickListener {
            Timber.d("[RtspVideoActivity] 暂停播放")
            mStates.pause()
        }
        
        // 恢复按钮
        binding.btnResume.setOnClickListener {
            Timber.d("[RtspVideoActivity] 恢复播放")
            mStates.resume()
        }
        
        // 停止按钮
        binding.btnStop.setOnClickListener {
            Timber.d("[RtspVideoActivity] 停止播放")
            mStates.stop()
        }
    }
    
    /**
     * 处理播放器状态变化
     * 根据不同状态更新 UI 显示
     */
    private fun handlePlayerState(state: RtspPlayerState) {
        val statusText = when (state) {
            is RtspPlayerState.Idle -> {
                getString(R.string.rtsp_status_idle)
            }
            is RtspPlayerState.Buffering -> {
                getString(R.string.rtsp_status_buffering)
            }
            is RtspPlayerState.Ready -> {
                getString(R.string.rtsp_status_ready)
            }
            is RtspPlayerState.Playing -> {
                getString(R.string.rtsp_status_playing)
            }
            is RtspPlayerState.Paused -> {
                getString(R.string.rtsp_status_paused)
            }
            is RtspPlayerState.Ended -> {
                getString(R.string.rtsp_status_ended)
            }
            is RtspPlayerState.Error -> {
                ToastUtils.showShort(state.message)
                getString(R.string.rtsp_status_error, state.message)
            }
        }
        
        binding.tvStatus.text = statusText
        Timber.d("[RtspVideoActivity] 播放状态: $statusText")
    }
    
    /**
     * Activity 暂停时暂停播放
     */
    override fun onPause() {
        super.onPause()
        mStates.pause()
        Timber.d("[RtspVideoActivity] Activity 暂停，暂停播放")
    }


    /**
     * Activity 销毁时停止播放
     */
    override fun onDestroy() {
        super.onDestroy()
        mStates.stop()
        Timber.d("[RtspVideoActivity] Activity 销毁，停止播放")
    }
    
    companion object {
        /**
         * Intent 参数：RTSP 地址
         */
        const val EXTRA_RTSP_URL = "rtsp_url"
        
        /**
         * Intent 参数：摄像头名称
         */
        const val EXTRA_CAMERA_NAME = "camera_name"
    }
}

