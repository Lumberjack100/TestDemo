package com.shmedo.lib.device.base.iot_cmd.parser.vms

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTResultParser

/**
 * 创建者:   gonghe <br></br>
 * 创建时间:  3/11/21 <br></br>
 * 描述：     解析网关终端设备遥测数据
 */
class TerminalTelemetryParser : IOTResultParser<String?> {
    override fun parse(result: String): String {
        val strs = result.split("&").toTypedArray()
        return strs[1]
    }

    override fun validate(result: String) {}
    override fun commandType(): IOTCommandType {
        return IOTCommandType.VMS_TERMINAL_QUERY_SAMPLE
    }
}