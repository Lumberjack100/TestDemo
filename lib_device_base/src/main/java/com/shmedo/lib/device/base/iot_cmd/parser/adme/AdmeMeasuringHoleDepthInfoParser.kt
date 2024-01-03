package com.shmedo.lib.device.base.iot_cmd.parser.adme

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeMeasuringHoleDepthInfo

/**
 * 创建者：gonghe
 * 创建时间：2024/1/2
 * 描述： TODO
 */
class AdmeMeasuringHoleDepthInfoParser: IOTCommandParser<AdmeMeasuringHoleDepthInfo> {
    override fun parseInstance(keyValueMap: Map<String, String>): AdmeMeasuringHoleDepthInfo {
        return AdmeMeasuringHoleDepthInfo().apply {
            morunstate = keyValueMap.getOrDefault("morunstate", morunstate)
            movementway = keyValueMap.getOrDefault("movementway", movementway)
            motorspeed = keyValueMap.getOrDefault("motorspeed", motorspeed)
            movedistance = keyValueMap.getOrDefault("movedistance", movedistance)
        }
    }
    override fun commandType(): IOTCommandType = IOTCommandType.ADME_MD_GET_MEASURING_HOLEDEPTH
}