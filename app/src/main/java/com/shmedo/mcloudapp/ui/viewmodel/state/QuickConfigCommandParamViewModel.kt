package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shmedo.core.model.DeviceCmdOrderInfo
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

/**
 * 快速配置指令参数页面的 ViewModel
 * 
 * @author: gonghe
 * @time: 2025/1/6
 * @desc: 管理配置数据、参数值和执行进度状态
 */
class QuickConfigCommandParamViewModel : ViewModel() {
    // 配置数据
    val cmdOrderInfo = MutableLiveData<DeviceCmdOrderInfo?>()

    // 空内容标识
    val emptyContent = NonNullObservableField(true)
    
    // 参数值映射（key: cmdEngName, value: 用户输入值）
    val parameterValues = mutableMapOf<String, String>()
    
    // 执行结果记录（用于导出）
    val executionResults = mutableListOf<CommandExecutionResult>()

    /**
     * 清理数据
     */
    fun clearData() {
        cmdOrderInfo.value = null
        parameterValues.clear()
        executionResults.clear()
    }
}

/**
 * 指令执行进度
 */
data class CommandExecutionProgress(
    val currentIndex: Int,      // 当前执行到第几条
    val totalCount: Int,         // 总共多少条指令
    val currentCommand: String,  // 当前执行的指令
    val status: ExecutionStatus, // 执行状态
    val successCount: Int = 0,   // 成功数量
    val failedCount: Int = 0,    // 失败数量
    val startTime: Long = 0L,    // 开始时间
)

/**
 * 指令执行结果（用于Excel导出）
 */
data class CommandExecutionResult(
    val command: String,
    val response: String,
    val success: Boolean,
    val errorMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 执行状态枚举
 */
enum class ExecutionStatus {
    PENDING,    // 待执行
    EXECUTING,  // 执行中
    SUCCESS,    // 成功
    ERROR,      // 错误
    COMPLETED   // 全部完成
}