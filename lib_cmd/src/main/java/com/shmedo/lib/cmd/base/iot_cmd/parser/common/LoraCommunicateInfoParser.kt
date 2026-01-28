package com.shmedo.lib.cmd.base.iot_cmd.parser.common

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.common.LoraCommunicateInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者：gonghe
 * 创建时间：2024/4/24
 * 描述： Lora通讯参数信息
 */
@IOTParser
class LoraCommunicateInfoParser : IOTCommandParser<LoraCommunicateInfo> {

    override fun parseKeyValueMap(keyValueMap: Map<String, String>): LoraCommunicateInfo {
        return LoraCommunicateInfo().apply {
            loratype = keyValueMap.getOrDefault("loratype", loratype)
            chl = keyValueMap.getOrDefault("chl", chl)
            outpwr = keyValueMap.getOrDefault("outpwr", outpwr)
            airbaud = keyValueMap.getOrDefault("airbaud", airbaud)
            netid = keyValueMap.getOrDefault("netid", netid)
            localid = keyValueMap.getOrDefault("localid", localid)
            dstid = keyValueMap.getOrDefault("dstid", dstid)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.MD_GET_LORA_CTRL

}