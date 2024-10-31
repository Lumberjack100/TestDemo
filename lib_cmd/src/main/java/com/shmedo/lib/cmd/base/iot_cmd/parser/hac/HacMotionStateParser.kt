package com.shmedo.lib.cmd.base.iot_cmd.parser.hac

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.hac.HacMotionState
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者：gonghe
 * 创建时间：2024/5/8
 * 描述： TODO
 */
@IOTParser
class HacMotionStateParser : IOTCommandParser<HacMotionState> {
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): HacMotionState {
        return HacMotionState().apply {
            abndiasis = keyValueMap.getOrDefault("abndiasis", abndiasis)
            measmode = keyValueMap.getOrDefault("measmode", measmode)
            motorinfo = keyValueMap.getOrDefault("motorinfo", motorinfo)
            measpoint = keyValueMap.getOrDefault("measpoint", measpoint)
            waittime = keyValueMap.getOrDefault("waittime", waittime)
            incvoltage = keyValueMap.getOrDefault("incvoltage", incvoltage)
            driveinputv = keyValueMap.getOrDefault("driveinputv", driveinputv)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.ADME_HAC_MD_GET_MOTION_STATE
}