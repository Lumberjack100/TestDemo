package com.shmedo.lib.cmd.base.iot_cmd.parser.adme

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.adme.AdmeMotionState
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/11
 *
 * 描述： TODO
 *
 *
 */
@IOTParser
class AdmeMotionStateParser : IOTCommandParser<AdmeMotionState> {
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): AdmeMotionState {
        return AdmeMotionState().apply {
            motionstate = keyValueMap.getOrDefault("motionstate", motionstate)
            inctiondis = keyValueMap.getOrDefault("inctiondis", inctiondis)
            measmode = keyValueMap.getOrDefault("measmode", measmode)
            motorinfo = keyValueMap.getOrDefault("motorinfo", motorinfo)
            measpoint = keyValueMap.getOrDefault("measpoint", measpoint)
            waittime = keyValueMap.getOrDefault("waittime", waittime)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.ADME_MD_GET_MOTION_STATE
}