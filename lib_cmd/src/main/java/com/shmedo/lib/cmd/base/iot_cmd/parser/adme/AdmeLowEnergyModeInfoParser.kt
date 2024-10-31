package com.shmedo.lib.cmd.base.iot_cmd.parser.adme

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.adme.AdmeLowEnergyModeInfo
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
class AdmeLowEnergyModeInfoParser: IOTCommandParser<AdmeLowEnergyModeInfo> {
    override fun parseKeyValueMap(keyValueMap: Map<String, String>): AdmeLowEnergyModeInfo {
        return AdmeLowEnergyModeInfo().apply {
            mode = keyValueMap.getOrDefault("mode", mode)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.ADME_MD_GET_LOW_ENERGY_MODE
}