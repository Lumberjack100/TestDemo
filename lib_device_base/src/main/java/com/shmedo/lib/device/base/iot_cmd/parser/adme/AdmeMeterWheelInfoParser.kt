package com.shmedo.lib.device.base.iot_cmd.parser.adme

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeMeterWheelInfo

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/13
 *
 * 描述： TODO
 *
 *
 */
class AdmeMeterWheelInfoParser: IOTCommandParser<AdmeMeterWheelInfo> {
    override fun parseInstance(keyValueMap: Map<String, String>): AdmeMeterWheelInfo {
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

    override fun commandType(): IOTCommandType = IOTCommandType.ADME_MD_GET_METER_WHEEL
}