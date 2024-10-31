package com.shmedo.lib.cmd.base.iot_cmd.parser.adme

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.adme.AdmeMeterWheelInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/13
 *
 * 描述： TODO
 *
 *
 */
@IOTParser
class AdmeMeterWheelInfoParser: IOTCommandParser<AdmeMeterWheelInfo> {
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): AdmeMeterWheelInfo {
        return AdmeMeterWheelInfo().apply {
            enclinenum = keyValueMap.getOrDefault("enclinenum", enclinenum)
            outline = keyValueMap.getOrDefault("outline", outline)
            uptiona = keyValueMap.getOrDefault("uptiona", uptiona)
            uptionb = keyValueMap.getOrDefault("uptionb", uptionb)
            upconstant = keyValueMap.getOrDefault("upconstant", upconstant)
            upfilter = keyValueMap.getOrDefault("upfilter", upfilter)
            downtiona = keyValueMap.getOrDefault("downtiona", downtiona)
            downtionb = keyValueMap.getOrDefault("downtionb", downtionb)
            downconstant = keyValueMap.getOrDefault("downconstant", downconstant)
            downfilter = keyValueMap.getOrDefault("downfilter", downfilter)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.ADME_MD_GET_METER_WHEEL
}