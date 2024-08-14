package com.shmedo.lib.cmd.base.iot_cmd.parser.common

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.common.LoraCommunicateInfo

/**
 * 创建者：gonghe
 * 创建时间：2024/4/24
 * 描述： Lora通讯参数信息
 */
class LoraCommunicateInfoParser : IOTCommandParser<LoraCommunicateInfo> {

    override fun parseInstance(keyValueMap: Map<String, String>): LoraCommunicateInfo {
        return LoraCommunicateInfo().apply {
            airbaud = keyValueMap.getOrDefault("airbaud", airbaud)
            chl = keyValueMap.getOrDefault("chl", chl)
            outpwr = keyValueMap.getOrDefault("outpwr", outpwr)
            netid = keyValueMap.getOrDefault("netid", netid)
            localid = keyValueMap.getOrDefault("localid", localid)
            dstid = keyValueMap.getOrDefault("dstid", dstid)
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.MD_GET_LORA_CTRL

}