package com.shmedo.lib.cmd.base.iot_cmd.parser.adme

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.adme.AdmeMeasuringHoleDepthInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者：gonghe
 * 创建时间：2024/1/2
 * 描述： TODO
 */
@IOTParser
class AdmeMeasuringHoleDepthInfoParser: IOTCommandParser<AdmeMeasuringHoleDepthInfo> {
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): AdmeMeasuringHoleDepthInfo {
        return AdmeMeasuringHoleDepthInfo().apply {
            morunstate = keyValueMap.getOrDefault("morunstate", morunstate)
            movementway = keyValueMap.getOrDefault("movementway", movementway)
            motorspeed = keyValueMap.getOrDefault("motorspeed", motorspeed)
            movedistance = keyValueMap.getOrDefault("movedistance", movedistance)
        }
    }
    override val commandType: IOTCommandType = IOTCommandType.ADME_MD_GET_MEASURING_HOLEDEPTH
}