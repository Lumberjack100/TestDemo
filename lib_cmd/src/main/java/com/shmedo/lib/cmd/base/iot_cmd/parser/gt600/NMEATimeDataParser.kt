package com.shmedo.lib.cmd.base.iot_cmd.parser.gt600

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.gt600.NMEATimeData
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * @author：gonghe
 * @time: 2026/1/8
 * @desc: NMEA 参数解析器
 *
 * 对应指令: md_getnmeatime
 * 应答示例: $cmd=md_getnmeatime&gga=1&rmc=1&vtg=1&gsv=1&gsa=1
 */
@IOTParser
class NMEATimeDataParser : IOTCommandParser<NMEATimeData> {

    override fun parseKeyValueMap(keyValueMap: Map<String, String>): NMEATimeData {
        return NMEATimeData().apply {
            gga = keyValueMap.getOrDefault("gga", gga)
            rmc = keyValueMap.getOrDefault("rmc", rmc)
            vtg = keyValueMap.getOrDefault("vtg", vtg)
            gsv = keyValueMap.getOrDefault("gsv", gsv)
            gsa = keyValueMap.getOrDefault("gsa", gsa)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.MD_GET_NMEA_TIME
}
