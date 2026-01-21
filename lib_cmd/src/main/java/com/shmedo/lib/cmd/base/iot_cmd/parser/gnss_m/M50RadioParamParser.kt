package com.shmedo.lib.cmd.base.iot_cmd.parser.gnss_m

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.gnss_m.M50RadioParam
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者：gonghe
 * 创建时间：2025/10/7
 * 描述：M50 电台参数解析
 */
@IOTParser
class M50RadioParamParser : IOTCommandParser<M50RadioParam> {
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): M50RadioParam {
        return M50RadioParam().apply {
            sw = keyValueMap.getOrDefault("sw", sw)
            freq_group = keyValueMap.getOrDefault("freq_group", freq_group)
            airbaud = keyValueMap.getOrDefault("airbaud", airbaud)
            txpower = keyValueMap.getOrDefault("txpower", txpower)
            local_addr = keyValueMap.getOrDefault("local_addr", local_addr)
            target_addr = keyValueMap.getOrDefault("target_addr", target_addr)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.M50_MD_RADIO_PARAM
}