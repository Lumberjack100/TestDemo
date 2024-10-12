package com.shmedo.lib.cmd.base.iot_cmd.parser.common

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.common.RadioCommunicateInfo

/**
 * 创建者：gonghe
 * 创建时间：2024/4/24
 * 描述： TODO
 */
class RadioCommunicateInfoParser : IOTCommandParser<RadioCommunicateInfo> {
    override fun parseInstance(keyValueMap: Map<String, String>): RadioCommunicateInfo {
        return RadioCommunicateInfo().apply {
            sw = keyValueMap.getOrDefault("sw", sw)
            airbaud = keyValueMap.getOrDefault("airbaud", airbaud)
            rxchl = keyValueMap.getOrDefault("rxchl", rxchl)
            txchl = keyValueMap.getOrDefault("txchl", txchl)
            outpwr = keyValueMap.getOrDefault("outpwr", outpwr)
            bcchl = keyValueMap.getOrDefault("bcchl", bcchl)
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.MD_GET_RADIO_CTRL
}