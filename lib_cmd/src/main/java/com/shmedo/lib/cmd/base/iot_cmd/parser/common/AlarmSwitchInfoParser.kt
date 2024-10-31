package com.shmedo.lib.cmd.base.iot_cmd.parser.common

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.common.AlarmSwitchInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者：gonghe
 * 创建时间：2024/5/7
 * 描述： TODO
 */
@IOTParser
class AlarmSwitchInfoParser: IOTCommandParser<AlarmSwitchInfo> {
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): AlarmSwitchInfo {
        return AlarmSwitchInfo().apply {
            sw = keyValueMap.getOrDefault("sw", sw)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.MD_GET_ALRAM_BROADCAST_SWITCH
}