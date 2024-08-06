package com.shmedo.lib.cmd.base.iot_cmd.utils

import com.shmedo.lib.cmd.base.iot_cmd.assemble.IOTCommandAssemble
import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType

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

        val cmd = result.substringBefore(IOTConstants.COMMAND_SPLICER)
            .removePrefix(IOTConstants.COMMAND_HEADER).trim()

        return IOTCommandType.entries.firstOrNull { it.toString() == cmd }
            ?: IOTCommandType.UNKNOWN_TYPE
    }

    fun getCommand(commandType: IOTCommandType): String {
        return IOTCommandAssemble<Any>(commandType).toString()
    }

    fun <T> getCommand(commandType: IOTCommandType, parameter: T): String {
        return IOTCommandAssemble(commandType, parameter).toString()
    }

    /**
     * 将英文错误原因转换为中文
     */
    fun convertErrorReason(
        reason: String,
        cmdType: IOTCommandType = IOTCommandType.COMMON_SETTING_COMMAND
    ): String {
        return when (reason) {
            "unsupported" -> "设备版本不支持"
            "state not ready" -> "状态未就绪"
            "equimodel_err" -> "设备模式错误"
            else -> {
                when (cmdType) {
                    IOTCommandType.MD_MR_SET_RS485_PORT1_SENSOR_PARAM -> {
                        when (reason) {
                            "swtoken" -> "水文标识错误"

                            else -> reason
                        }
                    }

                    else -> reason
                }
            }
        }
    }
}
