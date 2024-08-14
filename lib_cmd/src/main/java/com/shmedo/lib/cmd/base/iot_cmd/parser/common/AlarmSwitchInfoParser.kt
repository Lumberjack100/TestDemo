package com.shmedo.lib.cmd.base.iot_cmd.parser.common

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.common.AlarmSwitchInfo

/**
 * 创建者：gonghe
 * 创建时间：2024/5/7
 * 描述： TODO
 */
class AlarmSwitchInfoParser: IOTCommandParser<AlarmSwitchInfo> {
    override fun parseInstance(keyValueMap: Map<String, String>): AlarmSwitchInfo {
        return AlarmSwitchInfo().apply {
            sw = keyValueMap.getOrDefault("sw", sw)
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.MD_GET_ALRAM_BROADCAST_SWITCH
}