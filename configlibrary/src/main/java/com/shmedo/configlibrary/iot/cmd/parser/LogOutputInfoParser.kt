package com.shmedo.configlibrary.iot.cmd.parser

import com.shmedo.configlibrary.iot.enums.IOTCommandType
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser
import com.shmedo.configlibrary.iot.model.IotLogOutputInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/8/11 <br></br>
 * 描述：     解析设备日志输出方式信息
 */
class LogOutputInfoParser : IOTResultParser<IotLogOutputInfo?> {
    override fun parse(result: String): IotLogOutputInfo? {
        val info = IotLogOutputInfo()
        return try {
            val keyValues = result.split("&").toTypedArray()
            val keyValueMap = HashMap<String, String>()
            for (keyValue in keyValues) {
                val strs = keyValue.split("=").toTypedArray()
                if (strs.size < 2) {
                    keyValueMap[strs[0]] = ""
                } else {
                    keyValueMap[strs[0]] = strs[1]
                }
            }
            info.level = keyValueMap["level"]!!
            info.type = keyValueMap["type"]
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.GET_LOG_OUTPUT_MODE_LEVEL
    }
}