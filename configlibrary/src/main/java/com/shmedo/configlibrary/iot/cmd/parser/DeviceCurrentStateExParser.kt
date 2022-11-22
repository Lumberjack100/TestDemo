package com.shmedo.configlibrary.iot.cmd.parser

import com.shmedo.configlibrary.iot.enums.IOTCommandType
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/5/20 <br></br>
 * 描述：     解析运行状态数据(扩展指令)
 */
class DeviceCurrentStateExParser : IOTResultParser<String?> {
    override fun parse(result: String): String? {
        val info: String
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
            info = keyValueMap["status"]!!
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.QUERY_DEVICE_EX_STATUS
    }
}