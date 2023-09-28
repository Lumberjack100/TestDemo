package com.shmedo.lib.device.base.iot_cmd

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType


/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/22 <br/>
 * 描述：     把命令头、命令码和参数组装成相应的物联网指令
 */
class IOTCommandAssemble<T>(val commandType: IOTCommandType, private val parameters: T? = null) {

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
