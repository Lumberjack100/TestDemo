package com.shmedo.lib.cmd.base.iot_cmd.parser.adme

import com.shmedo.lib.cmd.base.iot_cmd.enums.IOTCommandType
import com.shmedo.lib.cmd.base.iot_cmd.interfaces.IOTCommandParser
import com.shmedo.lib.cmd.base.iot_cmd.model.adme.AdmeLockedRotorDetectionInfo
import com.shmedo.lib.cmd.base.iot_cmd.parser.IOTParser

/**
 * 创建者：gonghe
 *
 * 创建时间：2023/12/18
 *
 * 描述： TODO
 *
 *
 */
@IOTParser
class AdmeLockedRotorDetectionInfoParser: IOTCommandParser<AdmeLockedRotorDetectionInfo> {

    override fun parseKeyValueMap(keyValueMap: Map<String, String>): AdmeLockedRotorDetectionInfo {
        return AdmeLockedRotorDetectionInfo().apply {
            lowtbtss = keyValueMap.getOrDefault("lowtbtss", lowtbtss)
            numpput = keyValueMap.getOrDefault("numpput", numpput)
            pdajtime = keyValueMap.getOrDefault("pdajtime", pdajtime)
            detintiona = keyValueMap.getOrDefault("detintiona", detintiona)
            detintionb = keyValueMap.getOrDefault("detintionb", detintionb)
            lowtorblothr = keyValueMap.getOrDefault("lowtorblothr", lowtorblothr)
            lowtordetime = keyValueMap.getOrDefault("lowtordetime", lowtordetime)
            lowsusrana = keyValueMap.getOrDefault("lowsusrana", lowsusrana)
            lowsusranb = keyValueMap.getOrDefault("lowsusranb", lowsusranb)
            uptbtss = keyValueMap.getOrDefault("uptbtss", uptbtss)
            uptorblothr = keyValueMap.getOrDefault("uptorblothr", uptorblothr)
            uptordetime = keyValueMap.getOrDefault("uptordetime", uptordetime)
            upsusrana = keyValueMap.getOrDefault("upsusrana", upsusrana)
            upsusranb = keyValueMap.getOrDefault("upsusranb", upsusranb)
            holedepth = keyValueMap.getOrDefault("holedepth", holedepth)
            measpacing = keyValueMap.getOrDefault("measpacing", measpacing)
        }
    }

    override val commandType: IOTCommandType = IOTCommandType.ADME_MD_GET_LOCKED_ROTOR_DETECTION
}