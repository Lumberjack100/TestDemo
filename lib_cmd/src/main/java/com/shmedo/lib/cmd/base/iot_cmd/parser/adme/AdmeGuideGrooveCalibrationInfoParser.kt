package com.shmedo.lib.cmd.base.iot_cmd.parser.adme

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.adme.AdmeGuideGrooveCalibrationInfo

/**
 * 创建者：gonghe
 * 创建时间：2024/1/4
 * 描述： TODO
 */
class AdmeGuideGrooveCalibrationInfoParser : IOTCommandParser<AdmeGuideGrooveCalibrationInfo> {
    override fun parseInstance(keyValueMap: Map<String, String>): AdmeGuideGrooveCalibrationInfo {
        return AdmeGuideGrooveCalibrationInfo().apply {
            morunstate = keyValueMap.getOrDefault("morunstate", morunstate)
            movementway = keyValueMap.getOrDefault("movementway", movementway)
            motorspeed = keyValueMap.getOrDefault("motorspeed", motorspeed)
            movepulse = keyValueMap.getOrDefault("movepulse", movepulse)
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.ADME_MD_GET_GUIDE_GROOVE_CALIBRATION
}