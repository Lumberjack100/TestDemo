package com.shmedo.lib.device.base.iot_cmd.parser.adme

import com.shmedo.lib.device.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.device.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.device.base.iot_cmd.model.adme.AdmeLowEnergyModeInfo

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/15
 *
 * 描述： TODO
 *
 *
 */
class AdmeLowEnergyModeInfoParser: IOTCommandParser<AdmeLowEnergyModeInfo> {
    override fun parseInstance(keyValueMap: Map<String, String>): AdmeLowEnergyModeInfo {
        return AdmeLowEnergyModeInfo().apply {
            mode = keyValueMap.getOrDefault("mode", mode)
        }
    }

    override fun commandType(): IOTCommandType = IOTCommandType.ADME_MD_GET_LOW_ENERGY_MODE
}