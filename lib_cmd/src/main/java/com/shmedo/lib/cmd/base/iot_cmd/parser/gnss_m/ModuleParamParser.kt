package com.shmedo.lib.cmd.base.iot_cmd.parser.gnss_m

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.gnss_m.ModuleParam
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * @author：gonghe
 * @time: 2025/1/22
 * @desc: 模块参数解析器
 */
@IOTParser
class ModuleParamParser : IOTCommandParser<ModuleParam> {
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): ModuleParam {
        return ModuleParam(
            sw = keyValueMap.getOrDefault("sw", ""),
            autosw = keyValueMap.getOrDefault("autosw", ""),
            valid = keyValueMap.getOrDefault("valid", ""),
            borad_type = keyValueMap.getOrDefault("borad_type", ""),
            baud = keyValueMap.getOrDefault("baud", ""),
            data_type = keyValueMap.getOrDefault("data_type", ""),
            borad_soft = keyValueMap.getOrDefault("borad_soft", ""),
            rtcm_mqtt_sw = keyValueMap.getOrDefault("rtcm_mqtt_sw", ""),
            rtcm_log_sw = keyValueMap.getOrDefault("rtcm_log_sw", ""),
            nmea_log_sw = keyValueMap.getOrDefault("nmea_log_sw", ""),
            antenna_height = keyValueMap.getOrDefault("antenna_height", ""),
            altitude_angle = keyValueMap.getOrDefault("altitude_angle", "")
        )
    }

    override val commandType: IOTCommandType = IOTCommandType.MD_GET_ELEVATION_ANGLE
} 