package com.shmedo.lib.device.base.md_cmd.assemble

import com.shmedo.lib.device.base.md_cmd.enums.MDCommandType
import com.shmedo.lib.device.base.md_cmd.utils.MDConstants


/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2023/9/22 <br/>
 * 描述：     把命令头、命令码和参数组装成相应的物联网指令
 */
class MDCommandAssemble<T>(val commandType: MDCommandType, private val parameters: T? = null) {

    override fun toString(): String {
        return if (parameters == null) {
            MDConstants.SEND_COMMAND_HEADER + commandType
        } else {
            "${MDConstants.SEND_COMMAND_HEADER}$commandType$parameters${MDConstants.COMMAND_FOOTER}"
        }
    }
}
