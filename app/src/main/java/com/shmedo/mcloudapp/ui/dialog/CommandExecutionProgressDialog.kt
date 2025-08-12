package com.shmedo.mcloudapp.ui.dialog

import android.content.Context
import android.widget.ProgressBar
import android.widget.TextView
import com.lxj.xpopup.core.CenterPopupView
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.ui.viewmodel.state.CommandExecutionProgress
import com.shmedo.mcloudapp.ui.viewmodel.state.ExecutionStatus

/**
 * 指令执行进度对话框
 * 
 * @author: gonghe
 * @time: 2025/1/6
 * @desc: 显示指令执行的实时进度和状态
 */
class CommandExecutionProgressDialog(
    context: Context,
    private val totalCount: Int
) : CenterPopupView(context) {
    
    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgress: TextView
    private lateinit var tvCurrentCommand: TextView
    private lateinit var tvStatus: TextView
    
    override fun getImplLayoutId(): Int = R.layout.dialog_command_execution_progress
    
    override fun onCreate() {
        super.onCreate()
        initViews()
    }
    
    private fun initViews() {
        progressBar = findViewById(R.id.progressBar)
        tvProgress = findViewById(R.id.tv_progress)
        tvCurrentCommand = findViewById(R.id.tv_current_command)
        tvStatus = findViewById(R.id.tv_status)
        
        progressBar.max = 100
        progressBar.progress = 0
        tvProgress.text = "0/$totalCount"
        tvCurrentCommand.text = "准备执行..."
        tvStatus.text = "初始化"
    }
    
    /**
     * 更新进度
     */
    fun updateProgress(progress: CommandExecutionProgress) {
        val percentage = (progress.currentIndex * 100) / progress.totalCount
        progressBar.progress = percentage
        tvProgress.text = "${progress.currentIndex}/${progress.totalCount}"
        
        // 显示当前指令（最多显示50个字符）
        val displayCommand = if (progress.currentCommand.length > 50) {
            progress.currentCommand.take(50) + "..."
        } else {
            progress.currentCommand
        }
        tvCurrentCommand.text = displayCommand
        
        // 更新状态显示
        updateStatusDisplay(progress.status, progress.currentIndex, progress.totalCount)
    }
    
    private fun updateStatusDisplay(status: ExecutionStatus, current: Int, total: Int) {
        tvStatus.text = when (status) {
            ExecutionStatus.PENDING -> "准备中"
            ExecutionStatus.EXECUTING -> "执行中"
            ExecutionStatus.SUCCESS -> "成功"
            ExecutionStatus.ERROR -> "失败"
            ExecutionStatus.COMPLETED -> if (current == total) {
                "全部完成"
            } else {
                "部分完成"
            }
        }
        
        // 根据状态设置颜色
        val statusColor = when (status) {
            ExecutionStatus.SUCCESS, ExecutionStatus.COMPLETED -> 
                context.getColor(R.color.green)
            ExecutionStatus.ERROR -> 
                context.getColor(R.color.error_FF4400)
            else -> 
                context.getColor(R.color.colorPrimary)
        }
        tvStatus.setTextColor(statusColor)
    }
}
