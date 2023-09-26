package com.shmedo.lib.device.base.iot_cmd

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType

/**
 * 命令类，把命令头和命令码和参数组装成相应的命令
 */
class IOTCommand<T>(val commandType: IOTCommandType, private val parameters: T? = null) {

    companion object {
        const val COMMAND_HEADER = "\$cmd="
    }

    override fun toString(): String {
        return if (parameters == null) {
            COMMAND_HEADER + commandType
        } else {
            "$COMMAND_HEADER$commandType&$parameters"
        }
    }
}
