package com.shmedo.lib.cmd.base.iot_cmd.parser.gnss_m

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.gnss_m.GNSSRawData
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * @author：gonghe
 * @time: 2025/1/22
 * @desc: GNSS 原始数据解析器
 */
@IOTParser
class GNSSRawDataParser : IOTCommandParser<GNSSRawData> {
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): GNSSRawData {
        return GNSSRawData(
            ephes = keyValueMap.getOrDefault("ephes", ""),
            obs = keyValueMap.getOrDefault("obs", ""),
            msmp = keyValueMap.getOrDefault("msmp", ""),
            msmlevel = keyValueMap.getOrDefault("msmlevel", ""),
            obslevel = keyValueMap.getOrDefault("obslevel", ""),
            datatype = keyValueMap.getOrDefault("datatype", ""),
            mmpltype = keyValueMap.getOrDefault("mmpltype", ""),
            rtkthdtype = keyValueMap.getOrDefault("rtkthdtype", ""),
            adrthdtype = keyValueMap.getOrDefault("adrthdtype", "")
        )
    }

    override val commandType: IOTCommandType = IOTCommandType.MD_GET_SAMPLING_RATE
} 