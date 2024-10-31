package com.shmedo.lib.cmd.base.iot_cmd.parser.adme

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.adme.AdmeBrakePadControlInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/15
 *
 * 描述： TODO
 *
 *
 */
@IOTParser
class AdmeBrakePadControlInfoParser: IOTCommandParser<AdmeBrakePadControlInfo> {
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): AdmeBrakePadControlInfo {
        return AdmeBrakePadControlInfo().apply {
            mode = keyValueMap.getOrDefault("mode", mode)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.ADME_MD_GET_BRAKE_PAD_CONTROL
}