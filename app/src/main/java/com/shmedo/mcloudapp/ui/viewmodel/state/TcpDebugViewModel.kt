package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.TimeUtils
import com.kunminx.architecture.domain.message.MutableResult
import com.kunminx.architecture.domain.message.Result
import com.shmedo.core.model.DebugCmdLogInfo
import com.shmedo.lib.cmd.base.md_cmd.utils.MDConstants
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class TcpDebugViewModel : ViewModel() {
    val debugMode = NonNullObservableField("关")

    val tcpConnected = NonNullObservableField(false)

    // LiveData for observing log items
    private val _logItems = MutableResult<List<DebugCmdLogInfo>>()
    val logItems: Result<List<DebugCmdLogInfo>> = _logItems

    private val currentLogList = mutableListOf<DebugCmdLogInfo>()

    /**
     * 添加单条日志
     */
    fun addLog(cmdStr: String, colorRes: Int = ColorUtils.getColor(R.color.send_data_color)) {
        val tempLogList = mutableListOf<DebugCmdLogInfo>()

        val logInfo = DebugCmdLogInfo(
            logTime = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("HH:mm:ss.SSS")),
            content = cmdStr.replace(MDConstants.COMMAND_FOOTER, ""),
            colorRes = colorRes,
            byteCount = cmdStr.length
        )
        currentLogList.add(logInfo)
        tempLogList.add(logInfo)
        _logItems.value = tempLogList.toList()
    }

    /**
     * 批量添加日志
     */
    fun addLogBatch(logs: List<Pair<String, Int>>) {
        if (logs.isEmpty()) return

        val tempLogList = mutableListOf<DebugCmdLogInfo>()
        logs.forEach { (cmdStr, colorRes) ->
            val logInfo = DebugCmdLogInfo(
                logTime = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("HH:mm:ss.SSS")),
                content = cmdStr.replace(MDConstants.COMMAND_FOOTER, ""),
                colorRes = colorRes,
                byteCount = cmdStr.length
            )
            currentLogList.add(logInfo)
            tempLogList.add(logInfo)
        }
        
        // 批量更新后一次性通知
        _logItems.value = tempLogList.toList()
    }

    /**
     * 生成日志内容
     */
    fun generateLogContent(): String {
        return currentLogList.joinToString("\n") { "${it.logTime} ${it.content}" }
    }

    /**
     * 清空日志
     */
    fun clearLogs() {
        currentLogList.clear()
    }

    /**
     * 设置调试模式
     */
    fun setDebugMode(mode: String) {
        debugMode.set(mode)
    }

    /**
     * 设置TCP连接状态
     */
    fun setTcpConnected(connected: Boolean) {
        tcpConnected.set(connected)
    }
}