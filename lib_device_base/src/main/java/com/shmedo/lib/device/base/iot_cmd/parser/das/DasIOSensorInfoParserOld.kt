package com.shmedo.lib.device.base.iot_cmd.parser.das

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.OldIOTResultParser
import com.shmedo.lib.device.base.iot_cmd.model.das.DasIOSensorInfo

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2021/4/19 <br></br>
 * 描述：      解析DAS 开关量传感器参数
 */
class DasIOSensorInfoParserOld :
    OldIOTResultParser<DasIOSensorInfo?> {
    override fun parse(result: String): DasIOSensorInfo? {
        val info = DasIOSensorInfo()
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
            info.type = keyValueMap.getOrDefault("type", "NullKey")
            info.value = keyValueMap.getOrDefault("value", "NullKey")
            info.min_time = keyValueMap.getOrDefault("min_time", "NullKey")
            info
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.DAS_MD_GET_IO_SENSOR_INFO
    }
}