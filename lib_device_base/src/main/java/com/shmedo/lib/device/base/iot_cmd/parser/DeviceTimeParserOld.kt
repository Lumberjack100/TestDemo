package com.shmedo.lib.device.base.iot_cmd.parser

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.OldIOTResultParser
import com.shmedo.lib.device.base.iot_cmd.model.DeviceTimeInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/8/31 <br></br>
 * 描述：    解析设备终端时间
 */
class DeviceTimeParserOld :
    OldIOTResultParser<DeviceTimeInfo> {
    override fun parse(result: String): DeviceTimeInfo? {
        val info = DeviceTimeInfo()
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
            info.time = keyValueMap["time"]!!
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.QUERY_TERMINAL_TIME
    }
}