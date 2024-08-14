package com.shmedo.lib.cmd.base.iot_cmd.parser.adme

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.adme.AdmeMotorMotionDistanceInfo

/**
 * 创建者：gonghe
 * 创建时间：2024/1/2
 * 描述： TODO
 */
class AdmeMotorMotionDistanceInfoParser : IOTCommandParser<AdmeMotorMotionDistanceInfo> {
    override fun parseInstance(keyValueMap: Map<String, String>): AdmeMotorMotionDistanceInfo {
        return AdmeMotorMotionDistanceInfo().apply {
            pulsenumber = keyValueMap.getOrDefault("pulsenumber", pulsenumber)
            realmovedistance = keyValueMap.getOrDefault("realmovedistance", realmovedistance)
            realholedepth = keyValueMap.getOrDefault("realholedepth", realholedepth)
            recoholedepth = keyValueMap.getOrDefault("recoholedepth", recoholedepth)
            realmoveangle = keyValueMap.getOrDefault("realmoveangle", realmoveangle)
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.ADME_MD_GET_MEASURING_HOLEDEPTH_PULSE
}