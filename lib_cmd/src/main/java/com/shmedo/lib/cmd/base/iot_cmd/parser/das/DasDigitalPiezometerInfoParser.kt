package com.shmedo.lib.cmd.base.iot_cmd.parser.das

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.das.DasDigitalPiezometerInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者：gonghe
 * 创建时间：2024/1/30
 * 描述： TODO
 */
@IOTParser
class DasDigitalPiezometerInfoParserr : IOTCommandParser<DasDigitalPiezometerInfo> {

    override fun parseKeyValueMap(keyValueMap: Map<String, String>): DasDigitalPiezometerInfo {
        return DasDigitalPiezometerInfo().apply {
            sw = keyValueMap.getOrDefault("sw", sw)
            addr = keyValueMap.getOrDefault("addr", addr)
            threshold = keyValueMap.getOrDefault("threshold", threshold)
            corrval = keyValueMap.getOrDefault("corrval", corrval)
            ropelen = keyValueMap.getOrDefault("ropelen", ropelen)
            tubealti = keyValueMap.getOrDefault("tubealti", tubealti)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.DAS_MD_GET_DIGITAL_PIEZOMETER_INFO
}