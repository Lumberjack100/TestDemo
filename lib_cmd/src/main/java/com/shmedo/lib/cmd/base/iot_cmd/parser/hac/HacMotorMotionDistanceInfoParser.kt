package com.shmedo.lib.cmd.base.iot_cmd.parser.hac

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.hac.HacMotorMotionDistanceInfo

/**
 * 创建者：gonghe
 * 创建时间：2024/5/9
 * 描述： TODO
 */
class HacMotorMotionDistanceInfoParser : IOTCommandParser<HacMotorMotionDistanceInfo> {
    override fun parseInstance(keyValueMap: Map<String, String>): HacMotorMotionDistanceInfo {
        return HacMotorMotionDistanceInfo().apply {
            pulsenumber = keyValueMap.getOrDefault("pulsenumber", pulsenumber)
            realmovedistance = keyValueMap.getOrDefault("realmovedistance", realmovedistance)
            realholedepth = keyValueMap.getOrDefault("realholedepth", realholedepth)
            recoholedepth = keyValueMap.getOrDefault("recoholedepth", recoholedepth)
            abndiasis = keyValueMap.getOrDefault("abndiasis", abndiasis)
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.ADME_HAC_MD_GET_HOLE_MEASURE_PULSE

}