package com.shmedo.lib.device.base.iot_cmd.parser.common

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.device.base.iot_cmd.model.common.MudLevelMeterSensorInfo

/**
 * 创建者：gonghe
 * 创建时间：2024/4/26
 * 描述： TODO
 */
class MudLevelMeterSensorInfoParser: IOTCommandParser<MudLevelMeterSensorInfo> {
    override fun parseInstance(keyValueMap: Map<String, String>): MudLevelMeterSensorInfo {
        return MudLevelMeterSensorInfo().apply {
            height = keyValueMap.getOrDefault("height", height)
            gap = keyValueMap.getOrDefault("gap", gap)
            times = keyValueMap.getOrDefault("times", times)
            level = keyValueMap.getOrDefault("level", level)
            pixx = keyValueMap.getOrDefault("pixx", pixx)
            pixy = keyValueMap.getOrDefault("pixy", pixy)
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.MD_GET_MUD_LEVEL_METER_SENSOR
}