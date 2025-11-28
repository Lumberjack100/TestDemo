package com.shmedo.mcloudapp.ui.page.device.common

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.blankj.utilcode.util.ToastUtils
import com.kunminx.architecture.ui.page.DataBindingConfig
import com.shmedo.lib.rtsp.player.RtspPlayerState
import com.shmedo.mcloudapp.BR
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.baseclickproxy.BaseClickProxy
import com.shmedo.mcloudapp.databinding.FragmentRtspVideoBinding
import com.shmedo.mcloudapp.extensions.launchAndRepeatWithViewLifecycle
import com.shmedo.mcloudapp.extensions.nav
import com.shmedo.mcloudapp.extensions.registerOnBackPressedDispatcher
import com.shmedo.mcloudapp.ui.page.device.OptimizedBaseIOTDeviceFragment
import com.shmedo.mcloudapp.ui.viewmodel.state.RtspViewModel
import com.shmedo.mcloudapp.ui.viewmodel.state.ToolbarViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class RtspVideoFragment : OptimizedBaseIOTDeviceFragment() {
    private lateinit var binding: FragmentRtspVideoBinding
    private val toolbarViewModel: ToolbarViewModel by viewModels()
    private val mStates: RtspViewModel by viewModel()


    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(
            R.layout.fragment_rtsp_video,
            BR.toolbarVM,
            toolbarViewModel
        )
            .addBindingParam(BR.click, BaseClickProxy())
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = getBinding() as FragmentRtspVideoBinding
        binding.llToolbar.toolbar.title = getString(R.string.rtsp_video_player)
        binding.llToolbar.toolbar.setNavigationOnClickListener { v: View? ->
            nav().navigateUp()
        }
        registerOnBackPressedDispatcher {
            nav().navigateUp()
        }

        // 初始化播放器
        initPlayer()
        setupClickListeners()
    }

    private fun initPlayer() {
        // 绑定播放器到 PlayerView
        mStates.getPlayer().attachToPlayerView(binding.playerView)
    }

    override fun initData() {
        super.initData()

        // 获取摄像头名称（如果有）
        arguments?.getString(ARG_CAMERA_NAME)?.takeIf { it.isNotBlank() }?.let {
            binding.llToolbar.toolbar.title = it
        }

        // 获取 RTSP 地址（如果有）
        val initialUrl = arguments?.getString(ARG_RTSP_URL)?.trim()?.takeIf { it.isNotEmpty() }
        initialUrl?.let {
            binding.etRtspUrl.setText("rtsp://172.168.5.199/test.mkv")//it
            Timber.d("[RtspVideoFragment] 从参数获取 RTSP 地址: $it")
        }
    }

    override fun createObserver() {
        super.createObserver()
        launchAndRepeatWithViewLifecycle(Lifecycle.State.STARTED) {
            launch {
                // 观察播放器状态
                mStates.playerState.collect { state -> handlePlayerState(state) }
            }
            launch {
                // 观察视频尺寸
                mStates.videoSize.collect { (width, height) ->
                    if (width > 0 && height > 0) {
                        val info = getString(R.string.rtsp_video_resolution, width, height)
                        binding.tvVideoInfo.text = info
                        Timber.d("[RtspVideoFragment] 视频尺寸: ${width}x${height}")
                    }
                }
            }
            launch {
                // 观察播放进度（可选，用于调试）
                mStates.progress.collect { (position, duration) ->
                    if (duration > 0) {
                        Timber.v("[RtspVideoFragment] 播放进度: $position / $duration")
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        // 播放按钮
        binding.btnPlay.setOnClickListener {
            val inputUrl = binding.etRtspUrl.text?.toString()?.trim().orEmpty()

            if (inputUrl.isEmpty()) {
                ToastUtils.showShort(R.string.rtsp_error_empty_url)
                return@setOnClickListener
            }

            if (!inputUrl.startsWith("rtsp://")) {
                ToastUtils.showShort(R.string.rtsp_error_invalid_url)
                return@setOnClickListener
            }

            Timber.d("[RtspVideoFragment] 开始播放: $inputUrl")
            mStates.playStream(inputUrl)
        }

        // 暂停按钮
        binding.btnPause.setOnClickListener {
            Timber.d("[RtspVideoFragment] 暂停播放")
            mStates.pause()
        }

        // 恢复按钮
        binding.btnResume.setOnClickListener {
            Timber.d("[RtspVideoFragment] 恢复播放")
            mStates.resume()
        }

        // 停止按钮
        binding.btnStop.setOnClickListener {
            Timber.d("[RtspVideoFragment] 停止播放")
            mStates.stop()
        }
    }

    /**
     * 处理播放器状态变化
     * 根据不同状态更新 UI 显示
     */
    private fun handlePlayerState(state: RtspPlayerState) {
        val statusText = when (state) {
            is RtspPlayerState.Idle -> getString(R.string.rtsp_status_idle)
            is RtspPlayerState.Buffering -> getString(R.string.rtsp_status_buffering)
            is RtspPlayerState.Ready -> getString(R.string.rtsp_status_ready)
            is RtspPlayerState.Playing -> getString(R.string.rtsp_status_playing)
            is RtspPlayerState.Paused -> getString(R.string.rtsp_status_paused)
            is RtspPlayerState.Ended -> getString(R.string.rtsp_status_ended)
            is RtspPlayerState.Error -> {
                ToastUtils.showShort(state.message)
                getString(R.string.rtsp_status_error, state.message)
            }
        }

        binding.tvStatus.text = statusText
        Timber.d("[RtspVideoFragment] 播放状态: $statusText")
    }


    override fun onResume() {
        super.onResume()
    }


    override fun onPause() {
        super.onPause()
        if (isRemoving || requireActivity().isFinishing) {
            mStates.pause()
            Timber.d("[RtspVideoFragment] Fragment 即将退出界面，暂停播放")
        }
    }

    override fun onDestroyView() {
//        binding.playerView.player = null
        super.onDestroyView()
    }

    override fun handleCommandResponse(cmdStr: String) {
        Timber.d("[RtspVideoFragment] 收到设备指令响应: $cmdStr")
    }

    companion object {
        const val ARG_RTSP_URL = "rtsp_url"
        const val ARG_CAMERA_NAME = "camera_name"
    }
}
