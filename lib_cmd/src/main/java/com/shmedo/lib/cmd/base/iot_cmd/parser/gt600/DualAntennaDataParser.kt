package com.shmedo.lib.cmd.base.iot_cmd.parser.gt600

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.gt600.DualAntennaData
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * @author：gonghe
 * @time: 2026/1/8
 * @desc: 双天线参数解析器
 *
 * 对应指令: md_cfgnmeavtgout&method=0
 * 应答示例: $cmd=md_cfgnmeavtgout&method=0&switch=1&antdist=50.44&report_freq=1
 */
@IOTParser
class DualAntennaDataParser : IOTCommandParser<DualAntennaData> {

    override fun parseKeyValueMap(keyValueMap: Map<String, String>): DualAntennaData {
        return DualAntennaData().apply {
            method = keyValueMap.getOrDefault("method", method)
            switch = keyValueMap.getOrDefault("switch", switch)
            antdist = keyValueMap.getOrDefault("antdist", antdist)
            report_freq = keyValueMap.getOrDefault("report_freq", report_freq)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.MD_CFG_NMEA_VTG_OUT
}
