package com.shmedo.lib.device.base.iot_cmd.utils

import com.shmedo.lib.device.base.iot_cmd.IOTCommandAssemble
import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTConstants

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/8/31 <br></br>
 * 描述：     TODO #gh#
 */

object IOTCommandUtil {

    /**
     * 从指令响应结果中提取指令类型
     *
     * @param result $cmd=reqtime&time=2020-08-31 14:27:21&apikey=80c8b131-80e5-4504-bb67-72e8cf93a542&msgid=63d30270-80f6-4458-bafb-c669585f5e4b
     * @return 指令类型
     */
    fun extractCommandType(result: String): IOTCommandType {
        if (result.isBlank() || result.length < IOTConstants.RESULT_MIN_LENGTH) {
            return IOTCommandType.LENGTH_INVALID
        }

        val cmd = result.substringBefore("&").removePrefix(IOTConstants.COMMAND_HEADER).trim()

        return IOTCommandType.entries.firstOrNull { it.toString() == cmd } ?: IOTCommandType.UNKNOWN_TYPE
    }

    fun getCommand(commandType: IOTCommandType): String {
        return IOTCommandAssemble<Any>(commandType).toString()
    }

    fun <T> getCommand(commandType: IOTCommandType, parameter: T): String {
        return IOTCommandAssemble(commandType, parameter).toString()
    }
}
