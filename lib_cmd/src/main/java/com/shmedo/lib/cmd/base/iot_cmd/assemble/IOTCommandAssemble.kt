package com.shmedo.lib.cmd.base.iot_cmd.assemble

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.utils.IOTConstants


/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/22 <br/>
 * 描述：     把命令头、命令码和参数组装成相应的物联网指令
 */
class IOTCommandAssemble<T>(val commandType: IOTCommandType, private val parameters: T? = null) {
    override fun toString(): String {
        return if (parameters == null) {
            IOTConstants.COMMAND_HEADER + commandType
        } else {
            "${IOTConstants.COMMAND_HEADER}$commandType&$parameters"
        }
    }
}
