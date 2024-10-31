package com.shmedo.lib.cmd.base.iot_cmd.parser.adme

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.adme.AdmeBasicConfigInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者：gonghe
 * 创建时间：2023/12/21
 * 描述： TODO
 */
@IOTParser
class AdmeBasicConfigInfoParser : IOTCommandParser<AdmeBasicConfigInfo> {
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): AdmeBasicConfigInfo {
        return AdmeBasicConfigInfo().apply {
            inctype = keyValueMap.getOrDefault("inctype", inctype)
            address = keyValueMap.getOrDefault("address", address)
            interdeep = keyValueMap.getOrDefault("interdeep", interdeep)
            downspeed = keyValueMap.getOrDefault("downspeed", downspeed)
            downwaitetime = keyValueMap.getOrDefault("downwaitetime", downwaitetime)
            datatype = keyValueMap.getOrDefault("datatype", datatype)
        }
    }
    override val commandType: IOTCommandType = IOTCommandType.ADME_MD_GET_BASIC

}