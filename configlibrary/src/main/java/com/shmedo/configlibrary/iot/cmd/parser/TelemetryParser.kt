package com.shmedo.configlibrary.iot.cmd.parser

import com.shmedo.configlibrary.iot.enums.IOTCommandType
import com.shmedo.configlibrary.iot.interfaces.IOTResultParser

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  2020/8/31 <br></br>
 * 描述：     解析设备遥测数据
 */
class TelemetryParser : IOTResultParser<String?> {
    override fun parse(result: String): String {
        val strs = result.split("&").toTypedArray()
        return strs[1]
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.QUERY_SAMPLE
    }
}