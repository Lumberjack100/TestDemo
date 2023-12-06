package com.shmedo.lib.device.base.iot_cmd.parser.mr

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.device.base.iot_cmd.model.mr.MRSystemtime

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/5
 *
 * 描述： TODO
 *
 *
 */
class MRSystemtimeParser : IOTCommandParser<MRSystemtime> {
    override fun parseInstance(keyValueMap: Map<String, String>): MRSystemtime {
        return MRSystemtime().apply {
            time = keyValueMap.getOrDefault("time", time)
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.MD_MR_GET_SYSTEM_TIME
}