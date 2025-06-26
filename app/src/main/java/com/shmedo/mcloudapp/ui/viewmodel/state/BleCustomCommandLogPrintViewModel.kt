package com.shmedo.mcloudapp.ui.viewmodel.state

import androidx.lifecycle.ViewModel
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.TimeUtils
import com.kunminx.architecture.domain.message.MutableResult
import com.kunminx.architecture.domain.message.Result
import com.shmedo.core.model.DebugCmdLogInfo
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTCommandUtil
import com.shmedo.lib.cmd.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.cmd.base.md_cmd.enums.MDLogOutputStatus
import com.shmedo.lib.cmd.base.md_cmd.enums.MDWorkModel
import com.shmedo.lib.cmd.base.md_cmd.utils.MDCommandUtil
import com.shmedo.lib.cmd.base.md_cmd.utils.MDConstants
import com.shmedo.mcloudapp.R
import com.shmedo.mcloudapp.ui.page.base.viewmodel.NonNullObservableField

class BleCustomCommandLogPrintViewModel : ViewModel() {
    // Observable fields for data binding
    val debugMode = NonNullObservableField("关")
    val command = NonNullObservableField("")

    // LiveData for observing log items
    private val _logItems = MutableResult<List<DebugCmdLogInfo>>()
    val logItems: Result<List<DebugCmdLogInfo>> = _logItems

    // Command generation events
    private val _commandsToSend = MutableResult<List<String>>()
    val commandsToSend: Result<List<String>> = _commandsToSend

    private val currentLogList = mutableListOf<DebugCmdLogInfo>()

    enum class DebugMode(val displayName: String) {
        CLOSE("关"),
        DEBUG("debug模式"),
        INFO("info模式")
    }

    companion object {
        const val COMMAND_PREFIX_IOT = "\$cmd="
        const val COMMAND_PREFIX_MD_RAW = "\$cmd=md_raw&content="
        const val COMMAND_PREFIX_HASH = "##"
        
        val DEBUG_MODES = listOf("关", "debug模式", "info模式")
    }

    fun addLog(cmdStr: String, colorRes: Int = ColorUtils.getColor(R.color.send_data_color)) {
        val logInfo = DebugCmdLogInfo(
            logTime = TimeUtils.getNowString(TimeUtils.getSafeDateFormat("HH:mm:ss.SSS")),
            content = cmdStr.replace(MDConstants.COMMAND_FOOTER, ""),
            colorRes = colorRes,
            byteCount = cmdStr.length
        )
        currentLogList.add(logInfo)
        _logItems.value = currentLogList.toList()
    }

    fun setDebugMode(mode: String) {
        debugMode.set(mode)
    }

    fun updateCommand(newCommand: String) {
        command.set(newCommand)
    }

    fun generateLogContent(): String {
        return currentLogList.joinToString("\n") { "${it.logTime} ${it.content}" }
    }

    fun clearLogs() {
        currentLogList.clear()
        _logItems.value = emptyList()
    }

    fun validateCommand(command: String): Boolean {
        return command.isNotEmpty() && (
                command.startsWith(COMMAND_PREFIX_IOT) ||
                command.startsWith(COMMAND_PREFIX_HASH) ||
                command.contains("md_raw")
        )
    }

    fun processCommand(inputCommand: String): String {
        return if (inputCommand.startsWith(COMMAND_PREFIX_HASH)) {
            inputCommand.plus(MDConstants.COMMAND_FOOTER)
        } else {
            inputCommand
        }
    }

    fun generateDebugModeCommands(mode: DebugMode, isIotCmd: Boolean): List<String> {
        val commands = mutableListOf<String>()
        
        when (mode) {
            DebugMode.CLOSE -> {
                if (!isIotCmd) {
                    commands.add(MDCommandUtil.getCommand(
                        MDCommandType.LOG_OUTPUT_STATUS,
                        MDLogOutputStatus.CLOSE.toString()
                    ))
                    commands.add(MDCommandUtil.getCommand(
                        MDCommandType.WORK_MODE,
                        MDWorkModel.WORK.toString()
                    ))
                } else {
                    commands.add(IOTCommandUtil.getCommand(
                        IOTCommandType.MD_SET_LOG_OUTPUT_MODE_LEVEL,
                        "level=off&type=bt"
                    ))
                }
            }
            DebugMode.DEBUG -> {
                if (!isIotCmd) {
                    commands.add(MDCommandUtil.getCommand(
                        MDCommandType.LOG_OUTPUT_STATUS,
                        MDLogOutputStatus.OPEN.toString()
                    ))
                    commands.add(MDCommandUtil.getCommand(
                        MDCommandType.WORK_MODE,
                        MDWorkModel.DEBUG.toString()
                    ))
                } else {
                    commands.add(IOTCommandUtil.getCommand(
                        IOTCommandType.MD_SET_LOG_OUTPUT_MODE_LEVEL,
                        "level=debug&type=bt"
                    ))
                }
            }
            DebugMode.INFO -> {
                if (!isIotCmd) {
                    commands.add(MDCommandUtil.getCommand(
                        MDCommandType.LOG_OUTPUT_STATUS,
                        MDLogOutputStatus.OPEN.toString()
                    ))
                    commands.add(MDCommandUtil.getCommand(
                        MDCommandType.WORK_MODE,
                        MDWorkModel.INFO.toString()
                    ))
                } else {
                    commands.add(IOTCommandUtil.getCommand(
                        IOTCommandType.MD_SET_LOG_OUTPUT_MODE_LEVEL,
                        "level=info&type=bt"
                    ))
                }
            }
        }
        
        return commands
    }

    fun requestSendCommands(commands: List<String>) {
        _commandsToSend.value = commands
    }
}