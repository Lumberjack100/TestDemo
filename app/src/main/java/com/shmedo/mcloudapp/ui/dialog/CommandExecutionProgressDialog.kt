package com.shmedo.mcloudapp.ui.dialog

import android.animation.ValueAnimator
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import com.lxj.xpopup.core.CenterPopupView
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.databinding.DialogCommandExecutionProgressBinding
import com.shmedo.mcloudapp.ui.viewmodel.state.EnhancedCommandExecutionProgress
import com.shmedo.mcloudapp.ui.viewmodel.state.ExecutionStatus
import timber.log.Timber

/**
 * 指令执行进度对话框
 * 
 * @author: gonghe
 * @time: 2025/1/13
 * @desc: 提供进度展示和结果导出功能
 */
class CommandExecutionProgressDialog(
    context: Context,
    private val onExportExcel: (() -> Unit)? = null,
    private val onClose: (() -> Unit)? = null
) : CenterPopupView(context) {
    
    private lateinit var binding: DialogCommandExecutionProgressBinding
    private var startTime = System.currentTimeMillis()
    private val timeUpdateHandler = Handler(Looper.getMainLooper())
    private var timeUpdateRunnable: Runnable? = null
    private var isCompleted = false
    
    override fun getImplLayoutId(): Int = R.layout.dialog_command_execution_progress
    
    override fun onCreate() {
        super.onCreate()
        binding = DialogCommandExecutionProgressBinding.bind(contentView)
        initViews()
        startTimeUpdater()
    }
    
    private fun initViews() {
        // 初始化进度条
        binding.progressBar.max = 100
        binding.progressBar.progress = 0
        
        // 初始状态：隐藏操作按钮
        binding.llCompletionActions.visibility = View.GONE
        binding.btnExpandCommand.visibility = View.GONE
        
        // 设置按钮点击事件
        binding.btnClose.setOnClickListener {
            onClose?.invoke()
            dismiss()
        }
        
        binding.btnExportExcel.setOnClickListener {
            dismiss() // 先关闭对话框
            onExportExcel?.invoke()
        }
        
        binding.btnExpandCommand.setOnClickListener {
            toggleCommandExpansion()
        }
        
        // 初始显示
        updateTimeDisplay()
    }
    
    /**
     * 更新进度
     */
    fun updateProgress(progress: EnhancedCommandExecutionProgress) {
        // 动画更新进度条
        val targetProgress = if (progress.totalCount > 0) {
            (progress.currentIndex * 100) / progress.totalCount
        } else 0
        
        animateProgress(targetProgress)
        
        // 更新进度文本
        binding.tvProgressText.text = "${progress.currentIndex}/${progress.totalCount}"
        
        // 更新统计信息
        binding.tvStatistics.text = "成功:${progress.successCount} 失败:${progress.failedCount}"
        
        // 更新状态指示器
        updateStatusIndicator(progress.status)
        
        // 更新状态文本
        binding.tvStatus.text = getStatusText(progress.status)
        
        // 更新当前指令显示
        updateCurrentCommand(progress.currentCommand)
        
        // 检查是否完成
        if (progress.status == ExecutionStatus.COMPLETED) {
            onExecutionCompleted(progress)
        }
        
        Timber.d("进度更新: ${progress.currentIndex}/${progress.totalCount}, 状态: ${progress.status}")
    }
    
    private fun animateProgress(targetProgress: Int) {
        val currentProgress = binding.progressBar.progress
        ValueAnimator.ofInt(currentProgress, targetProgress).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                binding.progressBar.progress = animator.animatedValue as Int
            }
            start()
        }
    }
    
    private fun updateStatusIndicator(status: ExecutionStatus) {
        val color = when (status) {
            ExecutionStatus.EXECUTING -> R.color.colorPrimary
            ExecutionStatus.SUCCESS -> R.color.green
            ExecutionStatus.ERROR -> R.color.error_FF4400
            ExecutionStatus.COMPLETED -> R.color.green
            else -> R.color.gray_666666
        }
        
        binding.statusIndicator.backgroundTintList = ContextCompat.getColorStateList(context, color)
        
        // 添加脉冲动画效果
        if (status == ExecutionStatus.EXECUTING) {
            binding.statusIndicator.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(300)
                .withEndAction {
                    binding.statusIndicator.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(300)
                        .start()
                }
                .start()
        }
    }
    
    private fun getStatusText(status: ExecutionStatus): String {
        return when (status) {
            ExecutionStatus.PENDING -> "准备中"
            ExecutionStatus.EXECUTING -> "执行中"
            ExecutionStatus.SUCCESS -> "执行成功"
            ExecutionStatus.ERROR -> "执行失败"
            ExecutionStatus.COMPLETED -> "执行完成"
        }
    }
    
    private fun updateCurrentCommand(command: String) {
        if (command.isNotEmpty()) {
            binding.cardCurrentCommand.visibility = View.VISIBLE
            
            // 显示指令（最多显示100个字符）
            val displayCommand = if (command.length > 100) {
                binding.tvCurrentCommand.text = command.take(100) + "..."
                binding.btnExpandCommand.visibility = View.VISIBLE
                binding.tvCurrentCommand.tag = command // 保存完整指令
                command.take(100) + "..."
            } else {
                binding.tvCurrentCommand.text = command
                binding.btnExpandCommand.visibility = View.GONE
                command
            }
        }
    }
    
    private fun toggleCommandExpansion() {
        val fullCommand = binding.tvCurrentCommand.tag as? String ?: return
        val isExpanded = binding.tvCurrentCommand.maxLines == Integer.MAX_VALUE
        
        if (isExpanded) {
            binding.tvCurrentCommand.maxLines = 3
            binding.tvCurrentCommand.text = if (fullCommand.length > 100) {
                fullCommand.take(100) + "..."
            } else fullCommand
            binding.btnExpandCommand.text = "展开查看"
        } else {
            binding.tvCurrentCommand.maxLines = Integer.MAX_VALUE
            binding.tvCurrentCommand.text = fullCommand
            binding.btnExpandCommand.text = "收起"
        }
    }
    
    private fun onExecutionCompleted(progress: EnhancedCommandExecutionProgress) {
        isCompleted = true
        
        // 停止时间更新
        stopTimeUpdater()
        
        // 显示最终执行时间
        val elapsedTime = (System.currentTimeMillis() - startTime) / 1000
        val minutes = elapsedTime / 60
        val seconds = elapsedTime % 60
        binding.tvElapsedTime.text = "总耗时: ${String.format("%02d:%02d", minutes, seconds)}"
        
        // 更新标题
        binding.tvTitle.text = if (progress.failedCount == 0) {
            "配置成功"
        } else if (progress.successCount == 0) {
            "配置失败"
        } else {
            "部分成功"
        }
        
        // 显示完成图标动画
        playCompletionAnimation()
        
        // 显示操作按钮
        binding.llCompletionActions.visibility = View.VISIBLE
        
        // 隐藏当前指令卡片
        binding.cardCurrentCommand.visibility = View.GONE
        
        // 更新统计信息的样式
        val statsColor = if (progress.failedCount == 0) {
            R.color.green
        } else {
            R.color.error_FF4400
        }
        binding.tvStatistics.setTextColor(ContextCompat.getColor(context, statsColor))
    }
    
    private fun playCompletionAnimation() {
        // 成功动画
        binding.ivStatusIcon.animate()
            .scaleX(1.3f)
            .scaleY(1.3f)
            .setDuration(200)
            .withEndAction {
                binding.ivStatusIcon.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(200)
                    .start()
            }
            .start()
    }
    
    private fun startTimeUpdater() {
        timeUpdateRunnable = object : Runnable {
            override fun run() {
                if (!isCompleted) {
                    updateTimeDisplay()
                    timeUpdateHandler.postDelayed(this, 1000)
                }
            }
        }
        timeUpdateHandler.post(timeUpdateRunnable!!)
    }
    
    private fun stopTimeUpdater() {
        timeUpdateRunnable?.let { 
            timeUpdateHandler.removeCallbacks(it)
        }
    }
    
    private fun updateTimeDisplay() {
        val elapsedTime = (System.currentTimeMillis() - startTime) / 1000
        val minutes = elapsedTime / 60
        val seconds = elapsedTime % 60
        binding.tvElapsedTime.text = "已用时: ${String.format("%02d:%02d", minutes, seconds)}"
    }
    
    override fun onDismiss() {
        super.onDismiss()
        stopTimeUpdater()
    }
}
