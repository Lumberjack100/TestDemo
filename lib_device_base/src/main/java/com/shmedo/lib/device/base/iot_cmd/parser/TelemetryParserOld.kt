package com.shmedo.lib.device.base.iot_cmd.parser

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.OldIOTResultParser

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/8/31 <br></br>
 * 描述：     解析设备遥测数据
 */
class TelemetryParserOld :
    OldIOTResultParser<String?> {
    override fun parse(result: String): String {
        val strs = result.split("&").toTypedArray()
        return strs[1]
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.QUERY_SAMPLE
    }
}